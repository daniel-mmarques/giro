package com.giro.entites.email;

import com.giro.entites.enums.EmailStatusEnum;
import lombok.Data;
import org.springframework.context.annotation.Primary;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Primary
@Table(name = "EMAIL")
public class EmailModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "owner_ref")
    private String ownerRef;

    @Column(name = "email_from")
    private String emailFrom;

    @Column(name = "email_to")
    private String emailTo;

    @Column(name = "subject")
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "send_date")
    private LocalDateTime sendDateEmail;

    @Column(name = "email_status")
    @Enumerated(EnumType.STRING)
    private EmailStatusEnum emailStatus;

}

