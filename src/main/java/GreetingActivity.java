import com.uber.cadence.activity.Activity;

public interface GreetingActivity {
    void greet(String greeting);
}

class GreetingActivityImpl implements GreetingActivity {
    @Override
    public void greet(String greeting) {
        System.out.println("From " + Activity.getWorkflowExecution() + ": " + greeting);
    }
}