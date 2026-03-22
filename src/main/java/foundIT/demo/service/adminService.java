package foundIT.demo.service;


import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    /**getAdminById
     *  Gets the admin that corresponds to the specified id
     * 
     * @param id - The id of the admin to find
     * @return - Returns the admin if the id corresponds to it returns an empty optional variable if not
     */
    public Optional<admin> getAdminById(String id)
    {
        var admin = mongoTemplate.findById(id, admin.class);

        return Optional.ofNullable(admin);
    }

    /**getAdminByUsername
     *  Gets the admin that corresponds to the specified username
     * 
     * @param username - The username of the admin to find
     * @return - Returns the admin if the username corresponds to it returns an empty optional variable if not
     */
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

    /**updateAdmin
     *  Updates an admin in the database that corresponds to the given id
     * 
     * @param id - The id of the admin to change
     * @param admin - The updated info
     * @return - The updated admin
     */
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

    /**deleteAdmin
     *  Deletes an admin from the database
     * 
     * @param id - The id of the admin to delete
     * @return - Returns true if the the admin was successfully removed false if not
     */
    public boolean deleteAdmin(String id)
    {
        var query = new Query(Criteria.where("id").is(id));

        return mongoTemplate.remove(query, admin.class).getDeletedCount() > 0;
    }

    /**usernameExists
     *  Checks if the given username is in the admin table
     * 
     * @param ia - The admin info to check
     * @return - True if the username exists false if not
     */
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
    /**findPassword
     *  Checks through each admin for a corresponding username then returns the stored password
     * 
     * @param user - The username of the admin to find the password of
     * @return - The pasword if found null if not found
     */
    public String findPassword(String user)
    {

        
        for ( admin a : getAllAdmins())
        {
            if (a.getUsername().equals(user))
            {
                return a.getPassword();
            }
        }

        return null;

    }

    /**passwordMatches
     *  Checks if the inputted username and password correspond to a given admin
     * 
     * @param a - An admin object that stores the username and password
     * @return - True if the login is successful false if not
     */
    public boolean passwordMatches(admin a)
    {
        String rawPassword = a.getPassword();
        
        if (rawPassword == null || !usernameExists(a))
        {
            return false;
        }
        String storedPassword = findPassword(a.getUsername());

        return passwordEncoder.matches(rawPassword, storedPassword) || rawPassword.equals(storedPassword);
    }

    private boolean passwordMatches(String rawPassword, String storedPassword)
    {
        
        if (rawPassword == null || storedPassword == null || storedPassword.isBlank())
        {
            return false;
        }

        return passwordEncoder.matches(rawPassword, storedPassword) || rawPassword.equals(storedPassword);
    }




    
}
