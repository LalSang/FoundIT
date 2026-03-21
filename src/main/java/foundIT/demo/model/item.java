package foundIT.demo.model;

import java.time.Instant;

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

    //@NotBlank(message = "Date is Required")
    private Instant date;


    public item (String adminId, String itemType, String desc, String loc)
    {
        this.adminId = adminId;
        this.itemType = itemType;
        this.desc = desc;
        this.loc = loc;
        this.date = Instant.now();    
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

    public Instant getDate()
    {
        return this.date;
    }
    public void setDate(Instant date)
    {
        this.date = date;
    }

}
