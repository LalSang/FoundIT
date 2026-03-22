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

import foundIT.demo.model.foundItem;
import foundIT.demo.service.foundItemService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/foundItems")
public class foundItemController {
    

    private final foundItemService fIService;

    /**
     * @param fiS - fonundItemService for functions
     */
    public foundItemController(foundItemService fiS)
    {
        this.fIService = fiS;
    }

    @PostMapping
    // Create - Post /api/foundItems
    /**
     * creates foundItem
     * @param fI - foundItem object
     * @return - 201 if foundItem successfully created
     */
    public ResponseEntity<foundItem> createFoundItem(@Valid @RequestBody foundItem fI)
    {
        var savedFI = fIService.createFoundItem(fI);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFI);
    }

    // Read - Get /api/foundItems
    /**
     * returns foundItems
     * @return - 200 if successful
     */
    @GetMapping
    public ResponseEntity<List<foundItem>> getAllFoundItems()
    {
        List<foundItem> fI = fIService.getAllFoundItems();

        return ResponseEntity.status(HttpStatus.OK).body(fI);
    }

    // Read - Get /api/foundItems/{id}
    /**
     * finds foundItems
     * @param id - foundItem Identification
     * @return - 200 if successful
     */
    @GetMapping("/{id}")
    public ResponseEntity<foundItem> updateFoundItem(@PathVariable String id)
    {
        foundItem a = fIService.getFoundItemById(id).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body(a);
    }
    
    // Update - put /api/foundItems/{id}
    /**
     * updates foundItem
     * @param id - fonundItem Id
     * @param a - foundItem object
     * @return - 200 if good
     */
    @PutMapping("/{id}")
    public ResponseEntity<foundItem> updatedFI(@PathVariable String id,
                                                @RequestBody foundItem a)
    {
        if(!fIService.getFoundItemById(id).isPresent())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var updatedFoundItem = fIService.updateFoundItem(id, a);
        return ResponseEntity.status(HttpStatus.OK).body(updatedFoundItem);
    }

    // Delete - Delete /api/foundItems/{id}
    /**
     * deletes a foundItem
     * @param id - foundItem id
     * @return - 200 if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delteFoundItem(@PathVariable String id)
    {
        var deleted = fIService.deleteFoundItem(id);
        return deleted ? ResponseEntity.status(HttpStatus.OK).body("FoundItem Deleted Successfully") :
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete FonundItem");
    }

}
