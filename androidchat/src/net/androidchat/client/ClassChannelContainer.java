package net.androidchat.client;

import java.util.ArrayList;

public class ClassChannelContainer {

    public String channame;
    public String chantopic;
    public ArrayList<String> whatsinchan;
    public ArrayList<String> chanusers;
    public double loc_lat;
    public double loc_lng;
    
    public boolean NAMES_END;
    public boolean IS_STATUS;
    public boolean IS_PM;
    
    public ClassChannelContainer()
    {
        whatsinchan = new ArrayList<String>();
        chanusers = new ArrayList<String>();
        NAMES_END = true;
        IS_STATUS = false;
        IS_PM = false;
        chantopic = "No topic.";
    }

    public void addLine(String line)
    {
   	  if(line != "")
   	  if(line != null)
   		  whatsinchan.add(line);
        if (whatsinchan.size() > 30) // scrollback size
        {
            whatsinchan.remove(0);
        }
    }
}
