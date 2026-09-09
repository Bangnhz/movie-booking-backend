package com.moviebooking.movie_booking.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id","row_index","column_index"}))
@Getter
@Setter
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_index", nullable = false)
    private String rowIndex;

    @Column(name = "column_index", nullable = false)
    private Integer columnIndex;

    @ManyToOne
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatTypeEntity seatType;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @OneToMany(mappedBy = "seat")
    private List<SeatStatusEntity> statuses;
}