package org.elcer.accounts.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // ApacheDB can't handle AUTO
    private long id;

    private String name;
    private long balance;

    public Account(long id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public Account(long balance) {
        this.balance = balance;
    }

    public void subtractBalance(long balance) {
        this.balance -= balance;
    }

    public void increaseBalance(long balance) {
        this.balance += balance;
    }

}