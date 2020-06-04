package ezviz.ezopensdkcommon.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManager {

    private ExecutorService mExecutorService;

    public TaskManager(){
        mExecutorService = Executors.newCachedThreadPool();
    }

    public void submit(Runnable task){
        mExecutorService.submit(task);
    }

}
