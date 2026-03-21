package foundIT.demo.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;



@Document(collection = "items")
public class item {
    
    @Id private String id;

    @NotBlank(message = "Admin Id is Required")
    private String adminId;

    @NotBlank(message = "Item Type is Required")
    private String itemType;

    @NotBlank(message = "Description is Required")
    private String desc;

    @NotBlank(message = "Location is Required")
    private String loc;

    @NotBlank(message = "Date is Required")
    private String date;


    public item (String adminId, String itemType, String desc, String loc, String date)
    {
        this.adminId = adminId;
        this.itemType = itemType;
        this.desc = desc;
        this.loc = loc;
        this.date = date;
    }

    public String getAdminId()
    {
        return this.adminId;
    }
    public void setAdminId(String adminId)
    {
        this.adminId = adminId;
    }

    public String getItemType()
    {
        return this.itemType;
    }
    public void setItemType(String itemType)
    {
        this.itemType = itemType;
    }

    public String getDesc()
    {
        return this.desc;
    }
    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getLoc()
    {
        return this.loc;
    }
    public void setLoc(String loc)
    {
        this.loc = loc;
    }

    public String getDate()
    {
        return this.date;
    }
    public void setDate(String date)
    {
        this.date = date;
    }

}
