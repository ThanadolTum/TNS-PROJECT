package com.example.tnscpe;

public class AgentModel {

    String firstname;
    String lastname;
    Boolean status_receiver;
    Boolean verify;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Boolean getStatus_receiver() {
        return status_receiver;
    }

    public void setStatus_receiver(Boolean status_receiver) {
        this.status_receiver = status_receiver;
    }

    public Boolean getVerify() {
        return verify;
    }

    public void setVerify(Boolean verify) {
        this.verify = verify;
    }
}
