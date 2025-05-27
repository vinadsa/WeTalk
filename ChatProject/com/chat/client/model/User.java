package com.chat.client.model;

/**
 * Entity class representing a user in the database.
 * Used for authentication and user management.
 */
public class User {
    private int id;
    private String username;
    private String password;
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Constructor with username and password (for registration/login)
     * 
     * @param username The user's username
     * @param password The user's password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    /**
     * Full constructor with all fields
     * 
     * @param id The user's unique ID
     * @param username The user's username
     * @param password The user's password
     */
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
    
    /**
     * @return The user's ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * @param id The ID to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * @return The password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return id == user.id && 
               (username == null ? user.username == null : username.equals(user.username));
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }
}

