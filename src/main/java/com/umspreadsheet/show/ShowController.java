package com.umspreadsheet.show;

import com.umspreadsheet.criteria.SearchCriteria;
import com.umspreadsheet.helper.ControllerHelper;
import com.umspreadsheet.exception.DataNotFoundException;
import com.umspreadsheet.set.Set;
import com.umspreadsheet.set.SetDTO;
import com.umspreadsheet.review.TrackReview;
import com.umspreadsheet.review.TrackReviewForm;
import com.umspreadsheet.review.TrackReviewService;
import com.umspreadsheet.track.*;
import com.umspreadsheet.user.SimpleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.*;

@Controller
public class ShowController
{
    private ShowService showService;
    private TrackService trackService;
    private TrackReviewService trackReviewService;
    private SimpleUserService userService;

    @Autowired
    public ShowController(ShowService showService, TrackService trackService,
                          TrackReviewService trackReviewService, SimpleUserService userService)
    {
        this.showService = showService;
        this.trackService = trackService;
        this.trackReviewService = trackReviewService;
        this.userService = userService;
    }

    @RequestMapping("/shows/find")
    public String findShowFilter()
    {
        return "redirect:/shows#show-filter";
    }

    // Top-rated shows page
    @RequestMapping("/shows")
    public String topShowsPage(Model model)
    {
        model.addAttribute("topTwentyShows", setNumberOfReviews(showService.getTopTwentyShows()));

        return "/show/topShows";
    }

    @RequestMapping("/songs/find")
    public String findSongFilter()
    {
        return "redirect:/songs#song-filter";
    }

    // Returns a specified show's page
    @RequestMapping(value = "/shows/show", params = "showId", method = RequestMethod.GET)
    public String showPage(@RequestParam(value = "showId") Long showId, Model model) throws DataNotFoundException
    {
        String username = getCurrentUsername();
        Show show = showService.findById(showId);

        if (show == null)
            throw new DataNotFoundException("Show with id=" + showId + " not found.");

        // List of the show's track IDs to be used for Javascript dynamic slider creation
        Map<Long, Double> trackIdMap = new HashMap<>();

        // Create a map for the show where the string represents a set
        Map<String, Map<Track, TrackReviewForm>> trackAndReviewMap =
                getUsersTracksAndReviews(username, show, trackIdMap);

        model.addAttribute("trackIdMap", trackIdMap);
        model.addAttribute("trackAndReviewMap", trackAndReviewMap);
        model.addAttribute("show", showService.findById(showId));

        SetDTO setDTO = new SetDTO();
        setDTO.setShowId(showId);
        model.addAttribute("set", setDTO);

        model.addAttribute("allReviews", trackReviewService.getAllByShow(showId));

        if (!username.equals("anonymousUser"))
            model.addAttribute("trackReviewForm", new TrackReviewForm(username));

        return "/show/show";
    }

    // Delete a review
    @RequestMapping(value = "/shows/show", method = RequestMethod.DELETE)
    public String deleteTrackReview(@RequestParam("reviewId") Long reviewId,
                                    @RequestParam("showId") Long showId, RedirectAttributes redirectAttributes)
    {
        trackReviewService.delete(reviewId);

        redirectAttributes.addAttribute("showId", showId);
        redirectAttributes.addFlashAttribute("reviewDeleted", "true");

        return "redirect:/shows/show";
    }

    @RequestMapping("/shows/show/reviews")
    public String jumpToShowReviews(@RequestParam("showId") Long showId, RedirectAttributes redirectAttributes)
    {
        redirectAttributes.addAttribute("showId", showId);

        return "redirect:/shows/show#user-reviews";
    }

    @RequestMapping("/shows/random")
    public String randomShow(RedirectAttributes redirectAttributes)
    {
        redirectAttributes.addAttribute("showId", getRandomShow());

        return "redirect:/shows/show";
    }

    // Submit a new review
    @RequestMapping(value = "/shows/show", method = RequestMethod.POST)
    public String saveTrackReview(TrackReviewForm trackReviewForm, RedirectAttributes redirectAttributes)
    {
        TrackReview trackReview = new TrackReview();
        if (trackReviewForm.getId() != null)
            trackReview.setId(trackReviewForm.getId());

        trackReview.setScore(trackReviewForm.getScore());
        trackReview.setUser(userService.findByUsername(trackReviewForm.getUsername()));
        trackReview.setTrack(trackService.findById(trackReviewForm.getTrackId()));
        trackReview.setComment(trackReviewForm.getComment());

        TrackReview savedTrackReview = trackReviewService.save(trackReview);

        redirectAttributes.addAttribute("showId", savedTrackReview.getTrack().getShow().getId());
        redirectAttributes.addFlashAttribute("submitted", "true");

        return "redirect:/shows/show";
    }

    @RequestMapping(value = "/shows/search")
    public String submitShowFilter(@RequestParam(value = "year", required = false) String year,
                                   @RequestParam(value = "month", required = false) String month,
                                   @RequestParam(value = "day", required = false) String day,
                                   @RequestParam(value = "rating", required = false) String rating,
                                   Model model)
    {
        ShowSpecificationsBuilder builder = new ShowSpecificationsBuilder();

        if (year != null)
            builder.with("date", ":", year, SearchCriteria.DATE_SEGMENT_YEAR);
        if (month != null)
            builder.with("date", ":", month, SearchCriteria.DATE_SEGMENT_MONTH);
        if (day != null)
            builder.with("date", ":", day, SearchCriteria.DATE_SEGMENT_DAY);

        if (rating != null)
        {
            ControllerHelper.addRatingConstraints(rating, builder);
        }

        Specification<Show> specification = builder.build();
        List<Show> shows = showService.getShowsByFilter(specification);
        setNumberOfReviews(shows);

        model.addAttribute("showResults", shows);

        return "/show/showSearchResults";
    }

    private String getCurrentUsername()
    {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails)
        {
            username = ((UserDetails) principal).getUsername();
        } else
        {
            username = principal.toString();
        }

        return username;
    }

    private Map<String, Map<Track, TrackReviewForm>> getUsersTracksAndReviews(String username, Show show,
                                                                          Map<Long, Double> trackIdMap)
    {
        // Find the current user's reviews for the specified show
        List<TrackReview> trackReviews = trackReviewService.findByUserAndShow
                (show, userService.findByUsername(username));

        Map<String, Map<Track, TrackReviewForm>> trackAndReviewMap = new LinkedHashMap<>();
        // Loop through each set
        for (Set set : show.getSets())
        {
            // Create a map for the set
            Map<Track, TrackReviewForm> map = new LinkedHashMap<>();
            // Add the set name(String) and the map of its tracks/reviews
            trackAndReviewMap.put(set.getName(), map);

            // Loop through all tracks in the set
            for (Track track : set.getTracks())
            {
                // Set default score to null for Javascript slider dynamic setting
                Double score = null;

                // Create new trackReviewForm and set username via constructor
                TrackReviewForm trackReviewForm = new TrackReviewForm(username, track.getId());

                // Loop through user's reviews for this show
                for (TrackReview trackReview : trackReviews)
                {
                    // Check if the user has reviewed the track
                    if (track.getId().equals(trackReview.getTrack().getId()))
                    {
                        // If user has reviewed the track, populate a new trackReviewForm
                        trackReviewForm = new TrackReviewForm(trackReview);
                        score = trackReview.getScore();
                        break;
                    }
                }

                map.put(track, trackReviewForm);
                trackIdMap.put(track.getId(), score);
            }
        }

        return trackAndReviewMap;
    }

    private List<Show> setNumberOfReviews(List<com.umspreadsheet.show.Show> shows)
    {
        for (com.umspreadsheet.show.Show show : shows)
        {
            Long numberOfReviews = 0L;
            for (Set set: show.getSets())
            {
                for (Track track : set.getTracks())
                {
                    numberOfReviews += track.getReviews().size();
                }
            }
            show.setNumberOfReviews(numberOfReviews);
        }

        return shows;
    }

    private BigInteger getRandomShow()
    {
        List<BigInteger> idList = showService.findAllShowIds();
        return idList.get(new Random().nextInt(idList.size()));
    }
}