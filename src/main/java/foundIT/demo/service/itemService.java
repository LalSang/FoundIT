package foundIT.demo.service;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import foundIT.demo.model.foundItem;
import foundIT.demo.model.item;

@Service
public class itemService
{
    private final adminService aService;

    private final MongoTemplate mongoTemplate;


    /**itemService
     *  Class Constructor
     * 
     * @param mongoTemplate - The template for the mongo database
     * @param aService - adminService object so this class is able to run adminService functions
     */
    public itemService(MongoTemplate mongoTemplate, adminService aService)
    {
        this.mongoTemplate = mongoTemplate;
        this.aService = aService;
        
    }

    /**createItem
     *  Creates an item and adds it to the database
     * 
     * @param i - The item to be added
     * @return - The item if successfully added nothing if not
     */
    public item createItem(item i)
    {
        if((aService.getAdminById(i.getAdminId())).isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid admin");
        }

        if(i.getDate() == null)
        {
            i.setDate(Instant.now());
        }

        return mongoTemplate.save(i);
    }

    /**getAllItems
     *  Return a list of all items in the database
     * 
     * @return - A list of items
     */
    public List<item> getAllItems()
    {
        
        return mongoTemplate.findAll(item.class);
    }

    /**getType
     *  Theoretically returns all items of a certain type
     * 
     * @param type - The type of item to return
     * @return - A list of all items with the inputted type
     */
    public List<item> getType(String type)
    {
        Query query = new Query();
        query.addCriteria(Criteria.where("itemType").is(type));
        return  mongoTemplate.find(query, item.class);
    }

    /**getUnclaimedItems
     *  Finds all items that are in the item table but not in the foundItem table
     * 
     * @return - A list with all unclaimed items
     */
    public List<item> getUnclaimedItems()
    {
        List<foundItem> foundItems = mongoTemplate.findAll(foundItem.class);
        List<String> itemIds = new ArrayList<>();

        for (foundItem found : foundItems)
        {
            if (found.getItemId() != null && !found.getItemId().isBlank())
            {
                itemIds.add(found.getItemId());
            }
        }

        if (itemIds.isEmpty())
        {
            return getAllItems();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").nin(itemIds));
        return mongoTemplate.find(query, item.class);
    }

    // public List<item> getNotFound()
    // {
    //     List<foundItem> foundItems = mongoTemplate.findAll(foundItem.class);

    //     List<String> foundIds = foundItems.stream()
    //         .map(foundItem::getItemId)
    //         .toList();

    //     Query query = new Query();
    //     query.addCriteria(Criteria.where("_id").nin(foundIds));
    //     return  mongoTemplate.find(query, item.class);
    // }
    /**getClaimed
     *  Finds all items in the item table that have their item id saved in the foundItem table
     * 
     * @return - A list of all claimed items
     */
    public List<item> getClaimed()
    {
        List<foundItem> foundItems = mongoTemplate.findAll(foundItem.class);

        List<String> foundIds = foundItems.stream()
            .map(foundItem::getItemId)
            .toList();

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").in(foundIds));
        return  mongoTemplate.find(query, item.class);
    }

    


    /**getItemById
     *  Find the item with the corresponding id
     * 
     * @param id - Inputted id of the item
     * @return - Returns the item if the id exists an empty Optional variable if not
     */
    public Optional<item> getItemById(String id)
    {
        var item = mongoTemplate.findById(id, item.class);
        
        return Optional.ofNullable(item);
    }

    /**updateItem
     *  Updates a preexisting item
     * 
     * @param id - The Id of the item to update
     * @param i - The new info
     * @return - The updated item
     */
    public item updateItem(String id, item i)
    {
        if((aService.getAdminById(i.getAdminId())).isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid admin");
        }

        var query = new Query(Criteria.where("id").is(id));

        Update updatedItem = new Update()
                            .set("adminId", i.getAdminId())
                            .set("itemType", i.getItemType())
                            .set("desc", i.getDesc())
                            .set("category", i.getCategory())
                            .set("loc", i.getLoc())
                            .set("returnTo", i.getReturnTo())
                            .set("date", i.getDate());
        
        mongoTemplate.updateFirst(query, updatedItem, item.class);
        return getItemById(id).orElse(null);
    }

    /**deleteItem
     *  Deletes an item from the database
     * 
     * @param id - The id of the item to delete
     * @return - True if successful false if not
     */
    public boolean deleteItem(String id)
    {
        var query =new Query(Criteria.where("id").is(id));
        return mongoTemplate.remove(query, item.class).getDeletedCount() > 0;
    }

}
