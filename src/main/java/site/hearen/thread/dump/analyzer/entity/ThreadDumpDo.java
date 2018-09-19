package site.hearen.thread.dump.analyzer.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.hearen.thread.dump.analyzer.enums.DumpVersionEnum;
import site.hearen.thread.dump.analyzer.vo.ThreadDumpVo;

@Data
@Builder
@Entity
@Table(name = "thread_dump")
@AllArgsConstructor
@NoArgsConstructor
public class ThreadDumpDo implements Serializable {
    private static final long serialVersionUID = -1L;
    @Column(name = "parsed_dump_overview")
    String threadDumpVoJson;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dumpedTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date parsedTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "dump_version")
    private DumpVersionEnum version;
    private String details;
    private String createdBy;
    private String checkSum;
    @OneToMany(
            mappedBy = "dumpDo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ThreadDo> threadDoList;

    public ThreadDumpVo getVo() {
        Gson gson = new Gson();
        ThreadDumpVo threadDumpVo = gson.fromJson(this.getThreadDumpVoJson(), ThreadDumpVo.class);
        return threadDumpVo;
    }
}
