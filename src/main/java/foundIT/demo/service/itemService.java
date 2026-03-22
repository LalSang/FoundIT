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


    /** Wires the Mongo access layer and admin validation dependency. */
    public itemService(MongoTemplate mongoTemplate, adminService aService)
    {
        this.mongoTemplate = mongoTemplate;
        this.aService = aService;
        
    }

    /** Validates the admin, fills in a default timestamp, and saves a new item. */
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

    /** Returns every item currently stored in MongoDB. */
    public List<item> getAllItems()
    {
        
        return mongoTemplate.findAll(item.class);
    }

    /** Returns items whose saved item type matches the requested value. */
    public List<item> getType(String type)
    {
        Query query = new Query();
        query.addCriteria(Criteria.where("itemType").is(type));
        return  mongoTemplate.find(query, item.class);
    }

    /**
     * Returns only listings that do not already have a matching claim record.
     * The method first gathers claimed item ids from the found-item collection and
     * then excludes those ids from the item query.
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
    /** Returns items whose ids appear in the claim records collection. */
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

    


    /** Looks up a single item by id and wraps the result in an Optional. */
    public Optional<item> getItemById(String id)
    {
        var item = mongoTemplate.findById(id, item.class);
        
        return Optional.ofNullable(item);
    }

    /** Rewrites the editable fields for an existing item after admin validation. */
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

    /** Deletes an item by id and reports whether Mongo removed anything. */
    public boolean deleteItem(String id)
    {
        var query =new Query(Criteria.where("id").is(id));
        return mongoTemplate.remove(query, item.class).getDeletedCount() > 0;
    }

}
