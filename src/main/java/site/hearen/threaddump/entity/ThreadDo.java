package site.hearen.threaddump.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.hearen.threaddump.enums.StateEnum;
import site.hearen.threaddump.util.ListConverter;

@Data
@Builder
@Entity
@Table(name = "thread_info")
@AllArgsConstructor
@NoArgsConstructor
public class ThreadDo implements Serializable {
    private static final long serialVersionUID = -1L;
    String name;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "thread_dump_id")
    @JsonIgnore
    private ThreadDumpDo dumpDo;

    @Enumerated(EnumType.STRING)
    private StateEnum stateEnum;
    private boolean daemon;
    private boolean belongsToGc;
    private boolean belongsToFinalizer;
    private int priority;
    private int osPriority;
    private long threadId;
    private long nativeThreadId;

    @Convert(converter = ListConverter.class)
    private List<String> locksHeld;

    @Convert(converter = ListConverter.class)
    private List<String> locksWaiting;

    @Convert(converter = ListConverter.class)
    private List<String> callStack;

    @Convert(converter = ListConverter.class)
    private List<String> details;

}
