//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.jdownloader.extensions.schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import jd.controlling.JDLogger;

import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.extensions.schedule.modules.DisableClipboard;
import org.jdownloader.extensions.schedule.modules.DisablePremium;
import org.jdownloader.extensions.schedule.modules.DisablePremiumForHost;
import org.jdownloader.extensions.schedule.modules.DisableReconnect;
import org.jdownloader.extensions.schedule.modules.DoHibernate;
import org.jdownloader.extensions.schedule.modules.DoReconnect;
import org.jdownloader.extensions.schedule.modules.DoShutdown;
import org.jdownloader.extensions.schedule.modules.DoSleep;
import org.jdownloader.extensions.schedule.modules.EnableClipboard;
import org.jdownloader.extensions.schedule.modules.EnablePremium;
import org.jdownloader.extensions.schedule.modules.EnablePremiumForHost;
import org.jdownloader.extensions.schedule.modules.EnableReconnect;
import org.jdownloader.extensions.schedule.modules.PauseDownloads;
import org.jdownloader.extensions.schedule.modules.SetChunk;
import org.jdownloader.extensions.schedule.modules.SetMaxDownloads;
import org.jdownloader.extensions.schedule.modules.SetSpeed;
import org.jdownloader.extensions.schedule.modules.SetStopMark;
import org.jdownloader.extensions.schedule.modules.StartDownloads;
import org.jdownloader.extensions.schedule.modules.StopDownloads;
import org.jdownloader.extensions.schedule.modules.UnPauseDownloads;
import org.jdownloader.extensions.schedule.modules.UnSetStopMark;
import org.jdownloader.extensions.schedule.translate.T;

public class ScheduleExtension extends AbstractExtension<ScheduleConfig> {

    private ArrayList<Actions>                  actions;

    private ArrayList<SchedulerModuleInterface> modules;

    private SchedulerView                       view;

    private MainGui                             gui;

    private Schedulercheck                      sc      = null;

    private boolean                             running = false;

    public static final Object                  LOCK    = new Object();

    public ExtensionConfigPanel<ScheduleExtension> getConfigPanel() {
        return null;
    }

    public boolean hasConfigPanel() {
        return false;
    }

    public ScheduleExtension() throws StartException {
        super(T._.jd_plugins_optional_schedule_schedule());
    }

    private void initModules() {
        modules = new ArrayList<SchedulerModuleInterface>();
        try {
            modules.add(new DisableClipboard());
            modules.add(new DisablePremium());
            modules.add(new DisablePremiumForHost());
            modules.add(new DisableReconnect());
            modules.add(new DoHibernate());
            modules.add(new DoReconnect());
            modules.add(new DoShutdown());
            modules.add(new DoSleep());
            modules.add(new EnableClipboard());
            modules.add(new EnablePremium());
            modules.add(new EnablePremiumForHost());
            modules.add(new EnableReconnect());
            modules.add(new PauseDownloads());
            modules.add(new SetChunk());
            modules.add(new SetMaxDownloads());
            modules.add(new SetSpeed());
            modules.add(new SetStopMark());
            modules.add(new StartDownloads());
            modules.add(new StopDownloads());
            modules.add(new UnPauseDownloads());
            modules.add(new UnSetStopMark());
            Collections.sort(modules, new Comparator<SchedulerModuleInterface>() {
                public int compare(SchedulerModuleInterface o1, SchedulerModuleInterface o2) {
                    return o1.getTranslation().compareToIgnoreCase(o2.getTranslation());
                }
            });
        } catch (Throwable e) {
            JDLogger.exception(e);
        }
    }

    public ArrayList<SchedulerModuleInterface> getModules() {
        return modules;
    }

    public ArrayList<Actions> getActions() {
        return actions;
    }

    public void removeAction(int row) {
        if (row < 0) return;
        synchronized (LOCK) {
            actions.remove(row);
            saveActions();
        }
        updateTable();
    }

    public void addAction(Actions act) {
        synchronized (LOCK) {
            actions.add(act);
            saveActions();
        }
        updateTable();
    }

    public void saveActions() {
        synchronized (LOCK) {
            this.getPluginConfig().setProperty("Scheduler_Actions", actions);
            this.getPluginConfig().save();

            if (actions.size() == 0) {
                running = false;
            } else if (actions.size() > 0 && !sc.isAlive()) {
                running = true;
                sc = new Schedulercheck();
                sc.start();
            }
        }
    }

    public void updateTable() {
        if (sc != null && sc.isSleeping()) sc.interrupt();
        if (gui != null) gui.updateTable();
    }

    @Override
    public String getIconKey() {
        return "event";
    }

    public class Schedulercheck extends Thread {
        private Date                  today;
        private SimpleDateFormat      time;
        private SimpleDateFormat      date;
        private ArrayList<Actions>    tmpactions = null;
        private ArrayList<Executions> tmpexe     = null;
        private boolean               sleeping   = false;

        public Schedulercheck() {
            super("Schedulercheck");
            time = new SimpleDateFormat("HH:mm");
            date = new SimpleDateFormat("dd.MM.yyyy");
            tmpactions = new ArrayList<Actions>();
            tmpexe = new ArrayList<Executions>();
        }

        public boolean isSleeping() {
            synchronized (this) {
                return sleeping;
            }
        }

        private boolean updateTimer(Actions a, long curtime) {
            /* update timer of the action */
            if (a.getRepeat() == 0) {
                /* we do not have to update timers for disabled repeats */
            } else {
                /* we have to update timer */
                long timestamp = a.getDate().getTime();
                long currenttime = curtime;
                if (timestamp <= currenttime) {
                    a.setAlreadyHandled(false);
                    /* remove secs and milisecs */
                    currenttime = (currenttime / (60 * 1000));
                    currenttime = currenttime * (60 * 1000);
                    long add = a.getRepeat() * 60 * 1000l;
                    /* timestamp expired , set timestamp */
                    while (timestamp <= currenttime) {
                        timestamp += add;
                    }
                    Calendar newrepeat = Calendar.getInstance();
                    newrepeat.setTimeInMillis(timestamp);
                    a.setDate(newrepeat.getTime());
                    return true;
                }
            }
            return false;
        }

        @Override
        public void run() {
            try {
                logger.finest("Scheduler: start");
                while (running) {
                    logger.finest("Scheduler: checking");
                    /* getting current date and time */
                    long currenttime = System.currentTimeMillis();
                    today = new Date(currenttime);
                    String todaydate = date.format(today);
                    String todaytime = time.format(today);
                    boolean savechanges = false;
                    /* check all scheduler actions */
                    synchronized (LOCK) {
                        tmpactions.clear();
                        tmpactions.addAll(actions);
                    }
                    for (Actions a : tmpactions) {
                        /* check if we have to start the scheduler action */
                        if (a.isEnabled() && todaydate.equals(date.format(a.getDate())) && todaytime.equals(time.format(a.getDate()))) {
                            if (!a.wasAlreadyHandled()) {
                                a.setAlreadyHandled(true);
                                /* lets execute the action */
                                synchronized (LOCK) {
                                    tmpexe.clear();
                                    tmpexe.addAll(a.getExecutions());
                                }
                                for (Executions e : tmpexe) {
                                    logger.finest("Execute: " + e.getModule().getTranslation());
                                    e.exceute();
                                }
                            }
                        }
                        /* update timer */
                        if (updateTimer(a, currenttime)) savechanges = true;
                    }
                    if (savechanges) {
                        saveActions();
                    } else {
                        updateTable();
                    }
                    /* wait a minute and check again */
                    synchronized (this) {
                        sleeping = true;
                    }
                    try {
                        sleep(60000);
                    } catch (InterruptedException e) {
                    }
                    synchronized (this) {
                        sleeping = false;
                    }
                }
                logger.finest("Scheduler: stop");
            } catch (Exception e) {
                logger.severe("Scheduler: died!!");
                JDLogger.exception(e);
            }
        }
    }

    @Override
    protected void stop() throws StopException {
        saveActions();
        running = false;
        if (sc != null && sc.isSleeping()) sc.interrupt();
        sc = null;
    }

    @Override
    public boolean isQuickToggleEnabled() {
        return true;
    }

    @Override
    public boolean isDefaultEnabled() {
        return false;
    }

    @Override
    protected void start() throws StartException {
        actions = this.getPluginConfig().getGenericProperty("Scheduler_Actions", new ArrayList<Actions>());
        if (actions == null) {
            actions = new ArrayList<Actions>();
            saveActions();
        }

        initModules();

        logger.info("Schedule Init: OK");
        running = true;
        sc = new Schedulercheck();

        if (actions.size() > 0) {
            sc.start();
        }

    }

    @Override
    public String getConfigID() {
        return "scheduler";
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getDescription() {
        return T._.jd_plugins_optional_schedule_schedule_description();
    }

    @Override
    public SchedulerView getGUI() {
        return view;
    }

    @Override
    protected void initExtension() throws StartException {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                view = new SchedulerView(ScheduleExtension.this);
                gui = new MainGui(ScheduleExtension.this);
                view.setContent(gui);
            }
        };

    }
}