package com.c01.filebuilder;

public class Job {

  private String jobTitle;
  private String jobLink;
  private String companyName;
  private String companyLink;
  private String location;
  private String summery;

  public Job() {

  }

  public Job(String jobTitle, String jobLink, String companyName,
      String companyLink, String location, String summery) {
    this.jobTitle = jobTitle;
    this.jobLink = jobLink;
    this.companyName = companyName;
    this.companyLink = companyLink;
    this.location = location;
    this.summery = summery;
  }

  public String getCompanyLink() {
    return companyLink;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public String getLocation() {
    return location;
  }

  public String getSummery() {
    return summery;
  }

  public String getJobLink() {
    return jobLink;
  }

  public void setCompanyLink(String companyLink) {
    this.companyLink = companyLink;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setSummery(String summery) {
    this.summery = summery;
  }

  public void setJobLink(String jobLink) {
    this.jobLink = jobLink;
  }

  @Override
  public String toString() {
    String result = "";
    result += String.format("Job Title: %s\n", jobTitle);
    result += String.format("Job Link: %s\n", jobLink);
    result += String.format("Company Name: %s\n", companyName);
    result += String.format("Company Link: %s\n", companyLink);
    result += String.format("Location: %s\n", location);
    result += String.format("Summery: %s\n", summery);
    return result;
  }

}
