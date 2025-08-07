表结构
```sql
CREATE TABLE TaskCenter (
    taskCenterId BIGINT PRIMARY KEY COMMENT '主键',
    taskCenterIds JSON COMMENT '任务ID集合',
    roleType INT COMMENT '角色类型，see {@link EnumBusinessRoleType }',
    roleTypeBusinessId BIGINT COMMENT '角色对应的业务ID',
    taskName VARCHAR(255) COMMENT '任务名称',
    taskType INT COMMENT '任务类型：see {@link EnumTaskType}',
    taskTypes JSON COMMENT '任务类型集合：see {@link EnumTaskType}',
    taskContent TEXT COMMENT '任务内容的JSON串',
    status INT COMMENT '状态：see {@link EnumTaskStatus}',
    statusList JSON COMMENT '状态集合：see {@link EnumTaskStatus}',
    taskContentProcessResult TEXT COMMENT '任务内容处理结果，并不是每种类型都有这个值，taskType == 1 存储的是JSON串',
    statusRemark VARCHAR(255) COMMENT '状态备注',
    startProcessTime DATETIME COMMENT '开始处理时间',
    taskFinishTime DATETIME COMMENT '任务结束时间',
    operatorId BIGINT COMMENT '操作人id',
    operatorName VARCHAR(255) COMMENT '操作人名称',
    operatorPhone VARCHAR(20) COMMENT '操作人手机号',
    createTime DATETIME COMMENT '创建时间',
    updateTime DATETIME COMMENT '更新时间',
    preTaskId BIGINT COMMENT '前置任务id',
    afterTaskId BIGINT COMMENT '后置任务id',
    costTime BIGINT COMMENT '耗时（毫秒）',
    sortNo INT COMMENT '排序（越小越往前排）',
    total INT COMMENT '总数',
    current INT COMMENT '当前数',
    percentage INT COMMENT '进度百分比',
    parentTaskCenterId BIGINT COMMENT '关联的父任务ID 为空则为主任务'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务中心表';
```
定时任务(5s)扫描未完成的任务
```java
/**
 * 任务中心全局任务
 **/
@Slf4j
@Component
public class TaskCenterGlobalJobHandler {

			/**
       * 任务中心 Service
       */
      @DubboReference(group = "taskCenter", timeout = 300000)
      private TaskCenterService taskCenterService;
  
      @Resource
      private MQService mqService;
  
      /**
       * 键值服务
       */
      @DubboReference(group = "system")
      private KeyValueService keyValueService;
  
      /**
       * 执行等待任务 五秒钟执行一次
       **/
      @XxlJob(value = XxlJobRegisterMethodConstant.JOB_HANDLER_WAITING_TASK_CENTER_START_TASK)
      public void startWaitingTaskJobHandler() {
          long start = System.currentTimeMillis();
          try {
              List<TaskCenterBO> list = taskCenterService.listWaitChildTask();
              if (CollUtil.isEmpty(list)) {
                  return;
              }
  //            log.info("等待任务执行数：{}", list.size());
              List<TaskCenterUpdateStatusBO> taskCenterUpdateStatusBOList = new ArrayList<>();
              list.forEach(taskCenterBO -> {
  //                log.info("执行等待任务ID：{}", taskCenterBO.getTaskCenterId());
                  TaskCenterUpdateStatusBO updateStatusBO = new TaskCenterUpdateStatusBO();
                  updateStatusBO.setTaskCenterId(taskCenterBO.getTaskCenterId());
                  updateStatusBO.setStatus(EnumTaskStatus.PROCESSING.getValueCode());
                  taskCenterUpdateStatusBOList.add(updateStatusBO);
              });
              taskCenterService.updateBatchStatus(taskCenterUpdateStatusBOList);
  
              list.forEach(taskCenterBO -> {
                  mqService.sendNormalMsg(EnumMqMsgTag.TAG_TASK_CENTER_SINGLE_PROCESS_TASK, taskCenterBO, taskCenterBO.getParentTaskCenterId().toString());
              });
  
              XxlJobHelper.handleSuccess(String.format("execute success execute time:%sms", (System.currentTimeMillis() - start)));
          } catch (Exception e) {
              //记录错误日志
              ExceptionUtils.addExceptionLog(e);
              //将错误抛给XXLJOB进行后续处理
              throw e;
          }
      }
}
```

MqMessageListener
```java
/**
 * 消息枢纽
 */
public interface MqMessageListener<T> {

    /**
     * onMessage
     *
     * @param message 消息数据
     */
    MqConsumeResultDTO onMessage(T message);
}
```

TaskCenterSingeProcessListener
```java
/**
 * 任务中心 处理每一条任务
 * 统一由TaskCenterGlobalJobHandler.startTaskJobHandler 进行触发
 **/
@Slf4j
@Component
public class TaskCenterSingeProcessListener implements MqMessageListener<String>, InitializingBean {

    @DubboReference(group = "taskCenter")
    private TaskCenterService taskCenterService;

    @DubboReference(group = "taskCenter", timeout = 3600000)
    private TaskCenterStrategyService taskCenterStrategyService;

    @Override
    public MqConsumeResultDTO onMessage(String message) {
        MqConsumeResultDTO mqConsumeResult;
        TaskCenterBO taskCenterBO = JSONUtil.toBean(message, TaskCenterBO.class);
        if (taskCenterBO == null) {
            return MqConsumeResultDTO.fail("任务信息不能为空");
        }
        long start = System.currentTimeMillis();
        TaskCenterUpdateStatusBO updateStatusBO = new TaskCenterUpdateStatusBO();
        updateStatusBO.setTaskCenterId(taskCenterBO.getTaskCenterId());

        EnumTaskType enumTaskType = EnumTaskType.getEnumByType(taskCenterBO.getTaskType());
        try {
            CommonResult<TaskChildProcessResBO> commonResult = taskCenterStrategyService.doProcessingTaskByTaskType(enumTaskType.getType(), taskCenterBO);
            if (commonResult == null) {
                throw new BusinessException("任务执行结果不能为空");
            }
            mqConsumeResult = this.handleResult(updateStatusBO, commonResult, enumTaskType);
        } catch (Exception e) {
            MessageDTO messageDTO = ExceptionUtils.addExceptionLog(e);
            updateStatusBO.setStatus(EnumTaskStatus.FAILED.getValueCode());
            switch (enumTaskType) {
                case COLLECTION_GOODS_COPY_TO_SUPPLIER_GOODS:
                    updateStatusBO.setStatusRemark("基础信息处理失败，请重试！！");
                    break;
                default:
                    updateStatusBO.setStatusRemark(messageDTO.getMsg());
                    break;
            }
            mqConsumeResult = MqConsumeResultDTO.fail(ExceptionUtils.getStackTrace(e));
        } finally {
            // 更新执行任务结果
            long costTime = System.currentTimeMillis() - start;
            updateStatusBO.setCostTime(costTime);
            updateStatusBO.setTaskFinishTime(new Date());
            // 单条任务处理
            try {
                taskCenterStrategyService.doHandleTaskCenterSingeProcess(updateStatusBO, enumTaskType.getType());
            } catch (Exception e) {
                MessageDTO messageDTO = ExceptionUtils.addExceptionLog(e);

                updateStatusBO.setStatus(EnumTaskStatus.FAILED.getValueCode());
                updateStatusBO.setStatusRemark("任务结束处理异常：" + messageDTO.getMsg());
                taskCenterService.updateBatchStatus(updateStatusBO);
            }
        }
        return mqConsumeResult;
    }

    @Override
    public void afterPropertiesSet() {
        MqMessageFactory.register(EnumMqMsgTag.TAG_TASK_CENTER_SINGLE_PROCESS_TASK, this);
    }
}    
```

TaskChildHandleService
```java
/**
 * 子任务处理
 */
public interface TaskChildHandleService {

    /**
     * 执行任务
     **/
    CommonResult<TaskChildProcessResBO> processingChildTask(TaskCenterBO taskCenterBO);

    /**
     * 任务结束处理
     **/
    CommonResult<TaskChildFinishResBO> finishChildTask(TaskCenterBO taskCenterBO);
}

```











