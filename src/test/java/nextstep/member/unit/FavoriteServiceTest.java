package nextstep.member.unit;

import nextstep.favorite.application.FavoriteService;
import nextstep.favorite.application.dto.FavoriteRequest;
import nextstep.favorite.application.dto.FavoriteResponse;
import nextstep.favorite.domain.Favorite;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static nextstep.common.constants.ErrorConstant.INVALID_AUTHENTICATION_INFO;
import static nextstep.common.constants.ErrorConstant.NOT_FOUND_STATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
public class FavoriteServiceTest {

    private static final String EMAIL = "admin@email.com";

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private StationRepository stationRepository;

    private Station 강남역;
    private Station 역삼역;

    @BeforeEach
    void setUp() {
        // given
        강남역 = new Station("강남역");
        stationRepository.save(강남역);
        역삼역 = new Station("역삼역");
        stationRepository.save(역삼역);
    }

    @Test
    @DisplayName("경로 즐겨찾기 등록 실패-등록되지 않은 지하철역")
    void createFavorite_notEnrollStation() {
        // given
        final Station 선릉역 = new Station("선릉역");
        ReflectionTestUtils.setField(선릉역, "id", -1L);

        // when
        // then
        assertThatThrownBy(() -> favoriteService.saveFavorite(EMAIL, new FavoriteRequest(강남역.getId(), 선릉역.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(NOT_FOUND_STATION);
    }

    @Test
    @DisplayName("경로 즐겨찾기 등록")
    void createFavorite() {
        // when
        final Favorite favorite = favoriteService.saveFavorite(EMAIL, new FavoriteRequest(강남역.getId(), 역삼역.getId()));

        // then
        assertAll(
                () -> assertThat(favorite.getSource()).isEqualTo(강남역),
                () -> assertThat(favorite.getTarget()).isEqualTo(역삼역)
        );
    }

    @Test
    @DisplayName("즐겨찾기 조회")
    void showFavorites() {
        // given
        final Long id = favoriteService.saveFavorite(EMAIL, new FavoriteRequest(강남역.getId(), 역삼역.getId())).getId();

        // when
        final List<FavoriteResponse> favorites = favoriteService.showFavorites(EMAIL);

        // then
        assertAll(
                () -> assertThat(favorites.get(0).getId()).isEqualTo(id),
                () -> assertThat(favorites.get(0).getSource().getId()).isEqualTo(강남역.getId()),
                () -> assertThat(favorites.get(0).getTarget().getId()).isEqualTo(역삼역.getId())
        );
    }

    @Test
    @DisplayName("즐겨찾기 삭제 실패-다른 사용자의 즐겨찾기")
    void deleteFavorite_notMine() {
        // given
        final Long id = favoriteService.saveFavorite(EMAIL, new FavoriteRequest(강남역.getId(), 역삼역.getId())).getId();

        // when
        // then
        assertThatThrownBy(() -> favoriteService.deleteFavorite("member@email.com", id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_AUTHENTICATION_INFO);
    }

    @Test
    @DisplayName("즐겨찾기 삭제")
    void deleteFavorite() {
        // given
        final Long id = favoriteService.saveFavorite(EMAIL, new FavoriteRequest(강남역.getId(), 역삼역.getId())).getId();

        // when
        favoriteService.deleteFavorite(EMAIL, id);

        // then
        List<FavoriteResponse> favorites = favoriteService.showFavorites(EMAIL);
        assertThat(favorites.size()).isEqualTo(0);
    }
}
