package io.github.surajkumar;

public class User {
    private String forename;
    private String surname;
    private int age;
    private String location;

    public User() {}

    public User(String forename, String surname, int age, String location) {
        this.forename = forename;
        this.surname = surname;
        this.age = age;
        this.location = location;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
