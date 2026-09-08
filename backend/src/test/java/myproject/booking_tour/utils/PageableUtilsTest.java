package myproject.booking_tour.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * page va size di thang tu query string vao PageRequest.of(), va
 * /api/tours/search la endpoint cong khai - nen ba truong hop duoi day deu la
 * thu bat cu ai cung goi duoc khi khong co lop kep nay.
 */
class PageableUtilsTest {

    @Test
    void safeSize_ShouldCapAbsurdPageSizes() {
        // ?size=1000000 tren endpoint cong khai = mot lenh nap ca bang vao RAM.
        assertThat(PageableUtils.safeSize(1_000_000)).isEqualTo(PageableUtils.MAX_PAGE_SIZE);
        assertThat(PageableUtils.safeSize(Integer.MAX_VALUE)).isEqualTo(PageableUtils.MAX_PAGE_SIZE);
    }

    @Test
    void safeSize_ShouldReplaceNonPositiveWithDefault() {
        // PageRequest.of nem IllegalArgumentException voi so <= 0, va cai do
        // truoc day thanh 500 kem stack trace.
        assertThat(PageableUtils.safeSize(0)).isEqualTo(PageableUtils.DEFAULT_PAGE_SIZE);
        assertThat(PageableUtils.safeSize(-1)).isEqualTo(PageableUtils.DEFAULT_PAGE_SIZE);
        assertThat(PageableUtils.safeSize(Integer.MIN_VALUE)).isEqualTo(PageableUtils.DEFAULT_PAGE_SIZE);
    }

    @Test
    void safeSize_ShouldLeaveReasonableValuesAlone() {
        assertThat(PageableUtils.safeSize(1)).isEqualTo(1);
        assertThat(PageableUtils.safeSize(10)).isEqualTo(10);
        assertThat(PageableUtils.safeSize(PageableUtils.MAX_PAGE_SIZE)).isEqualTo(PageableUtils.MAX_PAGE_SIZE);
    }

    @Test
    void safePage_ShouldFloorNegativePagesAtZero() {
        assertThat(PageableUtils.safePage(-1)).isZero();
        assertThat(PageableUtils.safePage(Integer.MIN_VALUE)).isZero();
        assertThat(PageableUtils.safePage(0)).isZero();
        assertThat(PageableUtils.safePage(7)).isEqualTo(7);
    }
}
