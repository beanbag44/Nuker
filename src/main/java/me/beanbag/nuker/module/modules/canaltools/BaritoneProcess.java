package me.beanbag.nuker.module.modules.canaltools;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import me.beanbag.nuker.ModConfigs;


public class BaritoneProcess implements IBaritoneProcess {
    Goal goal;
    PathingCommandType commandType;
    boolean isActive = false;

    public BaritoneProcess() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess(this);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public PathingCommand onTick(boolean b, boolean isSafeToCancel) {
        if (goal.isInGoal(ModConfigs.INSTANCE.getMc().player.getBlockPos()) && isSafeToCancel) {
            return new PathingCommand(null, PathingCommandType.DEFER);
        }

        if (commandType == null && goal != null) {
            commandType = PathingCommandType.SET_GOAL_AND_PATH;
            return new PathingCommand(goal, PathingCommandType.SET_GOAL_AND_PATH);
        }
        if (commandType == null) {
            return new PathingCommand(null, PathingCommandType.DEFER);
        }
        return new PathingCommand(goal, commandType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public void onLostControl() {
        commandType = null;
        goal = null;
        isActive = false;
    }

    @Override
    public String displayName0() {
        return "Canal Tools Process";
    }


    public void pathToGoal(Goal goal) {
        this.goal = goal;
        commandType = null;
        isActive = true;
    }

    public void pauseBaritone() {
        isActive = true;
        this.commandType = PathingCommandType.REQUEST_PAUSE;
    }

    public void releaseControl() {
        onLostControl();
    }
}
