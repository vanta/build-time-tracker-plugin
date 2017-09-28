package net.rdrei.android.buildtimetracker

import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.internal.time.Time
import org.gradle.internal.time.Timer

class Timing {
    long ms
    String path
    boolean success
    boolean didWork
    boolean skipped

    Timing(long ms, String path, boolean success, boolean didWork, boolean skipped) {
        this.ms = ms
        this.path = path
        this.success = success
        this.didWork = didWork
        this.skipped = skipped
    }
}

class TimingRecorder extends BuildAndTaskExecutionListenerAdapter implements TaskExecutionListener {
    private Timer timer
    private List<Timing> timings = []
    private BuildTimeTrackerPlugin plugin

    TimingRecorder(BuildTimeTrackerPlugin plugin) {
        this.plugin = plugin
    }

    @Override
    void beforeExecute(Task task) {
        timer = Time.startTimer()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        timings << new Timing(
                timer.elapsedMillis,
                task.getPath(),
                taskState.getFailure() == null,
                taskState.getDidWork(),
                taskState.getSkipped()
        )
    }

    @Override
    void buildFinished(BuildResult result) {
        plugin.reporters.each { it.run timings; it.onBuildResult result }
    }

    List<String> getTasks() {
        timings*.path
    }

    Timing getTiming(String path) {
        timings.find { it.path == path }
    }
}
