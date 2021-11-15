package com.example.tnscpe;

public class MessagesModel {

    Data data = new Data();
    Receiver receiver = new Receiver();
    Sender sender = new Sender();
    String dateTime;
    Boolean read;
    Integer type;

    public MessagesModel() {
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public static class Data {

        String trackNumber;
        Receiver_Parcel receiver_parcel = new Receiver_Parcel();
        Owner owner = new Owner();

        public Data() {
        }

        public String getTrackNumber() {
            return trackNumber;
        }

        public void setTrackNumber(String trackNumber) {
            this.trackNumber = trackNumber;
        }

        public Receiver_Parcel getReceiver_parcel() {
            return receiver_parcel;
        }

        public void setReceiver_parcels(Receiver_Parcel receiver_parcels) {
            this.receiver_parcel = receiver_parcel;
        }

        public Owner getOwner() {
            return owner;
        }

        public void setOwner(Owner owner) {
            this.owner = owner;
        }

        public static class Receiver_Parcel {
            String firstname;
            String lastname;

            public Receiver_Parcel() {
            }

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

        }

        public static class Owner {
            String firstname;
            String lastname;

            public Owner() {
            }

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
        }
    }

    public static class Receiver {

        String firstname;
        String lastname;

        public Receiver() {
        }

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
    }

    public static class Sender {
        String firstname;
        String lastname;

        public Sender() {
        }

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
    }
}
