package site.hearen.threaddump.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "s3_info")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S3Info {
    @Id
    @GeneratedValue
    private Long id;
    String accessKey;
    String secretKey;
}
