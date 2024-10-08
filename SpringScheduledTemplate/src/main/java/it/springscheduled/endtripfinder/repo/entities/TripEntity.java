package it.springscheduled.endtripfinder.repo.entities;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Component
@Table(name = "lastEndTrip")
public class TripEntity {

    @Id
    @Column(name = "iddevice")
    private String idDevice;

    @Column(name = "lastdata")
    private Date lastData;

    @Column(name = "processed")
    private Integer processed;
}
