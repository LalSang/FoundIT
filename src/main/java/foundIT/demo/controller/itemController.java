package foundIT.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foundIT.demo.model.item;
import foundIT.demo.service.itemService;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/items")
public class itemController {
    

    private final itemService iService;


    /**
     * @param iService - itemService for functions
     */
    public itemController(itemService iService)
    {
        this.iService = iService;
    }

    // Create - Post /api/items
    /**
     * creates a itemObject
     * @param i - item object 
     * @return - 201 if successful
     */
    @PostMapping
    public ResponseEntity<item> createItem(@Valid @RequestBody item i)
    {
        var savedItem = iService.createItem(i);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    // Read - Get /api/items
    /**
     * returns all items
     * @return - 200 if successful
     */
    @GetMapping
    public ResponseEntity<List<item>> getAllItems()
    {
        List<item> itemList = iService.getAllItems();

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }

    // Read - Get /api/items/unclaimed
    /**
     * returns unclaimedItems
     * @return - 200 if successful
     */
    @GetMapping("/unclaimed")
    public ResponseEntity<List<item>> getUnclaimedItems()
    {
        List<item> itemList = iService.getUnclaimedItems();

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }

    // Read - Get /api/items/getType
    /**
     * finds item by type
     * @param type - item type for categorical search
     * @return  - 200 if successful
     */
    @GetMapping("/getType")
    public ResponseEntity<List<item>> getType(String type)
    {
        List<item> itemList = iService.getType(type);

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }

    // Read - Get /api/items/getNotClaimed
    // @GetMapping("/getNotClaimed")
    // public ResponseEntity<List<item>> getNotFound()
    // {
    //     List<item> itemList = iService.getNotFound();

    //     return ResponseEntity.status(HttpStatus.OK).body(itemList);
    // }

    // Read - Get /api/items/getClaimed
    /**
     * returns list of claimed items
     * @return - 200 if successful 
     */
    @GetMapping("/getClaimed")
    public ResponseEntity<List<item>> getClaimed()
    {
        List<item> itemList = iService.getClaimed();

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }


    // Read - Get /api/items/{id}
    /**
     * returns item by specific id
     * @param id - item identification number
     * @return - 200 if successful
     */
    @GetMapping("/{id}")
    public ResponseEntity<item> updateItem(@PathVariable String id)
    {
        item i = iService.getItemById(id).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body(i);
    }
    
    // Update - put /api/items/{id}
    /**
     * upates item 
     * @param id - item identification
     * @param i - item object
     * @return - 200 if successful
     */
    @PutMapping("/{id}")
    public ResponseEntity<item> updatedItem(@PathVariable String id,
                                                @RequestBody item i)
    {
        if(!iService.getItemById(id).isPresent())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var updatedItem = iService.updateItem(id, i);
        return ResponseEntity.status(HttpStatus.OK).body(updatedItem);
    }

    // Delete - Delete /api/items/{id}
    /**
     * deletes specific item
     * @param id - item id
     * @return - 200 if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable String id)
    {
        var deleted = iService.deleteItem(id);
        return deleted ? ResponseEntity.status(HttpStatus.OK).body("Item Deleted Successfully") :
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete Item");
    }

}
