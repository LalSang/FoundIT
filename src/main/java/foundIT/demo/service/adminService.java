package foundIT.demo.service;


import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.crypto.password.PasswordEncoder;

import foundIT.demo.model.admin;

@Service
public class adminService
{
    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;


    /** createAdmin
     *  Creates an admin and saves it to the Database
     * 
     * @param admin - The admin to be created
     * @return The newly saved Admin
     */

    public adminService(MongoTemplate mongoTemplate,  PasswordEncoder passwordEncoder)
    {
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public admin createAdmin(admin ad)
    {
        if(usernameExists(ad))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose another Username");
        }

        String encodedPassword = passwordEncoder.encode(ad.getPassword());
        ad.setPassword(encodedPassword);

        return mongoTemplate.save(ad);
    }

    /** getAdminById
     *  Gets the admin specified by the inputted id
     * 
     * @param id - The id of the admin to grab
     * @return - Returns an Optional object that either holds the admin if it exists
     *                  and empty if the admin does not exist
     */
    public List<admin> getAllAdmins()
    {
        return mongoTemplate.findAll(admin.class);
    }

    public Optional<admin> getAdminById(String id)
    {
        var admin = mongoTemplate.findById(id, admin.class);

        return Optional.ofNullable(admin);
    }

    public Optional<admin> getAdminByUsername(String username)
    {
        var query = new Query(Criteria.where("username").is(username));
        var admin = mongoTemplate.findOne(query, admin.class);

        return Optional.ofNullable(admin);
    }

    public Optional<admin> authenticate(String username, String password)
    {
        return getAdminByUsername(username)
                .filter(existingAdmin -> passwordMatches(password, existingAdmin.getPassword()));
    }

    public admin updateAdmin(String id, admin admin)
    {
        if(usernameExists(admin))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose another Username");
        }
        var query = new Query(Criteria.where("id").is(id));

        Update updatedAdmin = new Update()
                        .set("username", admin.getUsername())
                        .set("password", admin.getPassword())
                        .set("firstName", admin.getFirstName())
                        .set("lastName", admin.getLastName());        

        mongoTemplate.updateFirst(query, updatedAdmin, admin.class);

        return getAdminById(id).orElse(null);
    }


    public boolean deleteAdmin(String id)
    {
        var query = new Query(Criteria.where("id").is(id));

        return mongoTemplate.remove(query, admin.class).getDeletedCount() > 0;
    }


    public boolean usernameExists(admin ia)
    {
        List<admin> la = getAllAdmins();
        for ( admin a : la)
        {
            if (a.getUsername().equals(ia.getUsername()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword)
    {
        if (rawPassword == null || storedPassword == null)
        {
            return false;
        }

        return passwordEncoder.matches(rawPassword, storedPassword) || rawPassword.equals(storedPassword);
    }




    
}
