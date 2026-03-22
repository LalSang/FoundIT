package foundIT.demo.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;



@Document(collection = "items")
public class item {
    
    @Id private String id;

    // @NotBlank(message = "Admin Id is Required")
    private String adminId;

    @NotBlank(message = "Item Type is Required")
    private String itemType;

    @NotBlank(message = "Description is Required")
    private String desc;

    @NotBlank(message = "Category is Required")
    private String category;

    @NotBlank(message = "Location is Required")
    private String loc;

    @NotBlank(message = "Return desk is Required")
    private String returnTo;

    //@NotBlank(message = "Date is Required")
    private Instant date;

    /** Creates an empty item model for framework binding. */
    public item()
    {
    }

    /** Builds a new item record and timestamps it immediately. */
    public item (String adminId, String itemType, String desc, String category, String loc, String returnTo)
    {
        this.adminId = adminId;
        this.itemType = itemType;
        this.desc = desc;
        this.category = category;
        this.loc = loc;
        this.returnTo = returnTo;
        this.date = Instant.now();    
    }

    /** Returns the Mongo id for this item. */
    public String getId()
    {
        return this.id;
    }

    /** Sets the Mongo id for this item. */
    public void setId(String id)
    {
        this.id = id;
    }

    /** Returns the admin id that created the listing. */
    public String getAdminId()
    {
        return this.adminId;
    }

    /** Sets the admin id that owns the listing. */
    public void setAdminId(String adminId)
    {
        this.adminId = adminId;
    }

    /** Returns the user-facing item type or title. */
    public String getItemType()
    {
        return this.itemType;
    }

    /** Sets the user-facing item type or title. */
    public void setItemType(String itemType)
    {
        this.itemType = itemType;
    }

    /** Returns the saved description for the item. */
    public String getDesc()
    {
        return this.desc;
    }

    /** Sets the saved description for the item. */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    /** Returns the category assigned to the item. */
    public String getCategory()
    {
        return this.category;
    }

    /** Sets the category assigned to the item. */
    public void setCategory(String category)
    {
        this.category = category;
    }

    /** Returns the location where the item was found. */
    public String getLoc()
    {
        return this.loc;
    }

    /** Sets the location where the item was found. */
    public void setLoc(String loc)
    {
        this.loc = loc;
    }

    /** Returns the desk or office where the item should be returned. */
    public String getReturnTo()
    {
        return this.returnTo;
    }

    /** Sets the desk or office where the item should be returned. */
    public void setReturnTo(String returnTo)
    {
        this.returnTo = returnTo;
    }

    /** Returns the timestamp associated with the listing. */
    public Instant getDate()
    {
        return this.date;
    }

    /** Sets the timestamp associated with the listing. */
    public void setDate(Instant date)
    {
        this.date = date;
    }

}
