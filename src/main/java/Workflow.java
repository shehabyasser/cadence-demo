import com.uber.cadence.WorkflowIdReusePolicy;
import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.common.CronSchedule;
import com.uber.cadence.workflow.Workflow;
import com.uber.cadence.workflow.WorkflowMethod;

import java.time.Duration;

interface CronWorkflow {

    @WorkflowMethod(
            workflowId = Constants.CRON_WORKFLOW_ID,
            taskList = Constants.TASK_LIST,
            executionStartToCloseTimeoutSeconds = 30,
            workflowIdReusePolicy = WorkflowIdReusePolicy.AllowDuplicate
    )
    @CronSchedule("*/1 * * * *") // new workflow run every minute
    void greetPeriodically(String name);
}

class CronWorkflowImpl implements CronWorkflow {
    private final GreetingActivity activity = Workflow.newActivityStub(
            GreetingActivity.class,
            new ActivityOptions.Builder()
                    .setScheduleToCloseTimeout(Duration.ofSeconds(10))
                    .build());

    @Override
    public void greetPeriodically(String name) {
        activity.greet("Hello " + name + "!");
    }
}