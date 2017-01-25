package com.umspreadsheet.repository;

import com.umspreadsheet.domain.UmShow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ShowRepository extends CrudRepository<UmShow, Long>
{
    // SHOW WITH TRACKS ==================================================
    @Query("SELECT u FROM UmShow u JOIN FETCH u.tracks WHERE u.id = (:id)")
    public UmShow findByIdAndFetchTracksEagerly(@Param("id") Long id);

    @Query(value = "select um_show.* " +
            "from um_show " +
            "join " +
            "(select distinct track.show_id " +
            "from track " +
            "join track_review on track.id = track_review.track_id) as p where um_show.id = p.show_id",
            nativeQuery = true)
    public Iterable<UmShow> findAllWithReviews();

    public Iterable<UmShow> findByDateBetween(Date first, Date last);
}
