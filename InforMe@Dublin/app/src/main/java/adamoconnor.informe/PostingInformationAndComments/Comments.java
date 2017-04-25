package adamoconnor.informe.PostingInformationAndComments;


public class Comments {

    // declare strings of the firebase monument posts
    private String title;
    private String description;
    private String image;
    private String username;
    private String profile;

    // declare timestamp for monument post.
    private Long timestamp;

    // needed constructor.
    public Comments() {}

    /**
     * used as the overall constructor
     * @param title
     * setting title.
     * @param description
     * setting description.
     * @param image
     * setting image.
     * @param username
     * setting username.
     * @param timestamp
     * setting timestamp.
     * @param profile
     * setting profile image.
     */
    public Comments(String title, String description, String image, String username, Long timestamp, String profile) {

        this.title = title;
        this.description = description;
        this.image = image;
        this.username = username;
        this.timestamp = timestamp;
        this.profile = profile;
    }

    /**
     * getting timestamp
     * @return
     * timestamp value
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * setting timestamp
     * @param timestamp
     * timestamp value
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * getting profile Image
     * @return
     * profile image value
     */
    public String getProfile() {
        return profile;
    }

    /**
     * setting profile image
     * @param profile
     * profile image value
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * getting username
     * @return
     * username value
     */
    public String getUsername() {
        return username;
    }

    /**
     * setting timestamp
     * @param username
     * username value
     */
    public void setUsername(String username) { this.username = username;}

    /**
     * getting title
     * @return
     * title value
     */
    public String getTitle() {
        return title;
    }

    /**
     * setting titke
     * @param title
     * title value
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * getting description
     * @return
     * description value
     */
    public String getDescription() {
        return description;
    }

    /**
     * setting description
     * @param description
     * description value
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * getting image
     * @return
     * image value
     */
    public String getImage() {
        return image;
    }

    /**
     * setting image
     * @param image
     * image value
     */
    public void setImage(String image) {
        this.image = image;
    }


}
