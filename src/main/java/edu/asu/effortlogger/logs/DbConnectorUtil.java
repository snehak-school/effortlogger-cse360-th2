package edu.asu.effortlogger.logs;

import edu.asu.effortlogger.logs.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DbConnectorUtil {
    private final Connection dbConn;
    private final int uid = -1;

    public DbConnectorUtil(Connection dbConn) {
        this.dbConn = dbConn;
    }

    /* retrieves all Effort Categories from the database, and insert a list of their subordinates into each of them */
    public ArrayList<EffortCategory> getECs()
    {
        ArrayList<EffortCategory> effortCategoryList = new ArrayList<>();
        if (dbConn == null)
            return effortCategoryList;
        try {
            PreparedStatement allEffortCatPrepStatements = dbConn.prepareStatement("SELECT * FROM effortcat");
            PreparedStatement effortCatSubsPS = dbConn.prepareStatement("SELECT NAME FROM subordinates WHERE EC=?");
            ResultSet ecRSet = allEffortCatPrepStatements.executeQuery();
            while (ecRSet.next()){
                EffortCategory tmp = new EffortCategory(ecRSet.getString("NAME"));
                effortCatSubsPS.setInt(1, ecRSet.getInt("ID"));
                ResultSet ECSubsRSet = effortCatSubsPS.executeQuery();
                while (ECSubsRSet.next()) {
                    tmp.subs().add(new Subordinate(ECSubsRSet.getString("NAME")));
                }
                effortCategoryList.add(tmp);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return effortCategoryList;
    }

    /*retrieves a List of all Life Cycle Steps from the database */
    public ArrayList<LifeCycle> getLCSteps()
    {
        ArrayList<LifeCycle> ret = new ArrayList<>();
        if (dbConn == null) return ret;
        try {
            PreparedStatement allLCPS = dbConn.prepareStatement("SELECT * FROM lifecycle");
            ResultSet rs = allLCPS.executeQuery();
            while (rs.next()){
                ret.add(new LifeCycle(rs.getString("NAME"),
                        rs.getInt("EC") - 1,
                        rs.getInt("D") - 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /*retrieves all projects from the database as a List */
    public ArrayList<Project> getProjects()
    {
        PreparedStatement projectsPS = null;
        ArrayList<Project> ret = new ArrayList<>();
        if (dbConn == null)
            return null;
        try {
            ArrayList<LifeCycle> lcsteps = getLCSteps();
            projectsPS = dbConn.prepareStatement("SELECT * FROM projects");
            ResultSet rs =  projectsPS.executeQuery();
            while (rs.next()){
                ret.add(Project.createFrom(rs.getString("NAME"), rs.getString("lcSteps"), lcsteps));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /* inserts an effort log into the database */
    public void addEffortLog(LocalDateTime start, LocalDateTime stop, int p, int lifecycle, int EC, int subi, String substr)
    {
        if (dbConn == null) return;
        try {
            PreparedStatement elPS = dbConn.prepareStatement("INSERT INTO effortlogs (start, stop, project, lcstep, ec, subi, subt, user_id) values (?, ?, ?, ?, ?, ?, ?, ?);");
            elPS.setString(1, start.toString());
            elPS.setString(2, stop.toString());
            elPS.setInt(3, p + 1);
            elPS.setInt(4, lifecycle + 1);
            elPS.setInt(5, EC + 1);
            elPS.setInt(6, subi + 1);
            elPS.setString(7, substr);
            elPS.setInt(8, uid);

            elPS.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* insert effort log into a database with a specific id (Primary Key) */
    public void addEffortLog(int ID, LocalDateTime start, LocalDateTime stop, int p, int lifecycle, int EC, int subi, String substr)
    {
        if (dbConn == null) return;
        try {
            PreparedStatement elPS = dbConn.prepareStatement("INSERT INTO effortlogs (start, stop, project, lcstep, ec, subi, subt, user_id, ID) values (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            elPS.setString(1, start.toString());
            elPS.setString(2, stop.toString());
            elPS.setInt(3, p + 1);
            elPS.setInt(4, lifecycle + 1);
            elPS.setInt(5, EC + 1);
            elPS.setInt(6, subi + 1);
            elPS.setString(7, substr);
            elPS.setInt(8, uid);
            elPS.setInt(9, ID);

            elPS.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* adds an effort log with no stop time, to be resumed on next app start */
    public void addPartialEffortLog(LocalDateTime start, int p, int lifecycle, int EC, int subi, String substr)
    {
        if (dbConn == null) return;
        try {
            PreparedStatement elPS = dbConn.prepareStatement("INSERT INTO effortlogs (start, stop, project, lcstep, ec, subi, subt, user_id) values (?, ?, ?, ?, ?, ?, ?, ?);");
            elPS.setString(1, start.toString());
            elPS.setString(2, "-");
            elPS.setInt(3, p + 1);
            elPS.setInt(4, lifecycle + 1);
            elPS.setInt(5, EC + 1);
            elPS.setInt(6, subi + 1);
            elPS.setString(7, substr);
            elPS.setInt(8, uid);

            elPS.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* retrieve the ID of the last effort log from the user. used to check if the last effort log is a partial log. */
    public int getLastEffortLogID()
    {
        int ret = -1;
        try {
            PreparedStatement getMaxIDPS = dbConn.prepareStatement("SELECT MAX(ID) FROM effortlogs WHERE user_id = ?");
            getMaxIDPS.setInt(1, uid);
            ResultSet rs = getMaxIDPS.executeQuery();
            if (rs.next())
                return  rs.getInt(1);
            else
                return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /* returns the string form of the stop time of the last effort log of the user. used to check if the last effort log is a partial log. */
    public String getLastEffortLogStop()
    {
        String ret = "X";
        try {
            PreparedStatement elPS = dbConn.prepareStatement("SELECT stop FROM effortlogs WHERE ID = ?");
            int id = getLastEffortLogID();
            if (id == -1) return "X";
            elPS.setInt(1, id);
            ResultSet rs = elPS.executeQuery();
            if (rs.next())
                ret = rs.getString("STOP");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    // Checks for partial log
    public boolean isPartialEffortLog()
    {
        try {
            String stop = getLastEffortLogStop();
            return stop.equals("-");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* returns the partial log of the logged in user */
    public EffortLog getPartialLog()
    {
        int id = getLastEffortLogID();
        if (id == -1) return null;
        EffortLog ef = null;
        try {
            PreparedStatement elPS = dbConn.prepareStatement("SELECT * FROM effortlogs WHERE ID = ?");
            elPS.setInt(1, id);
            ResultSet rs = elPS.executeQuery();
            if (rs.next()){
                ef = new EffortLog(
                        LocalDateTime.parse(rs.getString("START")),
                        null,
                        rs.getInt("PROJECT") - 1,
                        rs.getInt("LCSTEP") - 1,
                        rs.getInt("EC") - 1,
                        rs.getInt("SUBI") - 1,
                        rs.getString("SUBT"),
                        rs.getInt("ID")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ef;
    }

    /* returns a List of all non-partial effort logs retrieved from the database*/
    public ArrayList<EffortLog> getEffortLogs(int project)
    {
        project++;
        ArrayList<EffortLog> ret = new ArrayList<>();
        if (dbConn == null) return ret;
        try {
            PreparedStatement elPS = dbConn.prepareStatement("SELECT * FROM effortlogs WHERE PROJECT = ?");
            elPS.setInt(1, -1);
            elPS.setInt(1, project);
            ResultSet rs = elPS.executeQuery();
            while (rs.next()){
                if (rs.getString("stop").equals("-")) {
                    return ret;
                }
                ret.add(new EffortLog(
                        LocalDateTime.parse(rs.getString("start")),
                        LocalDateTime.parse(rs.getString("stop")),
                        rs.getInt("project") - 1,
                        rs.getInt("lcStep") - 1,
                        rs.getInt("ec") - 1,
                        rs.getInt("subi") - 1,
                        rs.getString("subt"),
                        rs.getInt("ID")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    /** updates an Effort Log in the database where the Primary key matches ID */
    public void alterEL(int ID, String start, String end, int LCSTEP, int EC, int SUBI, String SUBS)
    {
        if (dbConn == null) {return;}
        try {
            PreparedStatement alterELPS = dbConn.prepareStatement("UPDATE effortlogs SET start = ?, stop = ?, lcStep = ?, ec = ?, subi = ?, subt = ? WHERE id = ?");
            alterELPS.setString(1, start);
            alterELPS.setString(2, end);
            alterELPS.setInt(3, LCSTEP + 1);
            alterELPS.setInt(4, EC + 1);
            alterELPS.setInt(5, SUBI + 1);
            alterELPS.setString(6, SUBS);
            alterELPS.setInt(7, ID);
            alterELPS.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** delete an Effort Log from the database where the Primary Key matches ID */
    public void deleteEffortLog(int id)
    {
        if (dbConn == null) {return;}
        try {
            PreparedStatement delPS = dbConn.prepareStatement("DELETE FROM effortlogs WHERE id = ?");
            delPS.setInt(1, id);
            delPS.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** deletes all effort logs from the effort log table that are part of the project foreign key */
    public void deleteAllEffortLogsFromProj(int project)
    {
        project++;
        if (dbConn == null) {return;}
        try {
            PreparedStatement delallPS = dbConn.prepareStatement("DELETE FROM effortlogs WHERE project = ?");
            delallPS.setInt(1, project);
            delallPS.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** increments the ID (Primary Key) of all Effort Logs, where the ID is greater the the parameter ID */
    public void incEffortLogIds(int beginId)
    {
        if (dbConn == null) {return;}
        try {
            PreparedStatement incPS = dbConn.prepareStatement("UPDATE effortlogs SET ID = ID + 1 WHERE ID > ?");
            incPS.setInt(1, beginId);
            incPS.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** updates the end time of an Effort Log in the database */
    public void updateEnd(int effortLogId, String end)
    {
        if (dbConn == null) {return;}
        try {
            PreparedStatement upEndPS = dbConn.prepareStatement("UPDATE effortlogs SET stop = ? WHERE id = ?");
            upEndPS.setString(1, end);
            upEndPS.setInt(2, effortLogId);
            upEndPS.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** returns a List of all Defects from a project in a Database */
    public ArrayList<Defect> getDefects(int project)
    {
        project++;
        ArrayList<Defect> ret = new ArrayList<>();
        if (dbConn == null) {return ret;}
        try {
            PreparedStatement defPS = dbConn.prepareStatement("SELECT * FROM defects WHERE project = ?");
            defPS.setInt(1, project);
            ResultSet rs = defPS.executeQuery();

            while (rs.next()){
                ret.add(new Defect(rs.getInt("id"), project - 1, rs.getString("name"), rs.getInt("open") == 1, rs.getString("info"), rs.getInt("lcInject"), rs.getInt("lcRemove"), rs.getInt("category"), rs.getInt("fixDefect")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /** creates a new Defect in the database */
    public int createDefect(int project)
    {
        project++;
        if (dbConn == null) {
            return -1;
        }
        try {
            PreparedStatement cdPS = dbConn.prepareStatement("INSERT INTO defects (project) VALUES (?)");
            cdPS.setInt(1, project);
            cdPS.executeUpdate();
            PreparedStatement maxIdPs = dbConn.prepareStatement("SELECT MAX(id) FROM defects");
            ResultSet rs = maxIdPs.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** removes a defect from the database */
    public void deleteDefect(int id)
    {
        if (dbConn == null) {
            return;
        }
        try {
            PreparedStatement delPS = dbConn.prepareStatement("DELETE FROM defects WHERE id = ?");
            delPS.setInt(1, id);
            delPS.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** updates a defect in the database where the Primary Key matches d.id */
    public void updateDefect(Defect d)
    {
        if (dbConn == null) {
            return;
        }
        try {
            PreparedStatement upDPS = dbConn.prepareStatement("UPDATE defects SET name = ?, open = ?, info = ?, lcInject = ?, lcRemove = ?, category = ?, fixDefect = ? WHERE id = ?");
            upDPS.setString(1, d.name());
            upDPS.setInt(2, d.open() ? 1 : 0);
            upDPS.setString(3, d.info());
            upDPS.setInt(4, d.lcInject());
            upDPS.setInt(5, d.lcRemove());
            upDPS.setInt(6, d.category());
            upDPS.setInt(7, d.fixDefect());
            upDPS.setInt(8, d.id());
            upDPS.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** removes all defects from a database where project matches the project foreign key */
    public void purgeProjDefects(int project)
    {
        project++;
        if (dbConn == null) {
            return;
        }
        try {
            PreparedStatement ppdPS = dbConn.prepareStatement("DELETE FROM defects WHERE project = ?");
            ppdPS.setInt(1, project);
            ppdPS.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
