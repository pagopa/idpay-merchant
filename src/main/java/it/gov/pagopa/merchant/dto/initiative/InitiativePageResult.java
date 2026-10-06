package it.gov.pagopa.merchant.dto.initiative;

import java.util.Collections;
import java.util.List;

public class InitiativePageResult {

    private List<InitiativeResponse> data;
    private List<CountWrapper> totalCount;

    public List<InitiativeResponse> getData() {
        return data != null ? data : Collections.emptyList();
    }

    public void setData(List<InitiativeResponse> data) {
        this.data = data;
    }

    public List<CountWrapper> getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(List<CountWrapper> totalCount) {
        this.totalCount = totalCount;
    }

    public long getTotal() {
        if (totalCount == null || totalCount.isEmpty()) {
            return 0L;
        }
        return totalCount.get(0).getCount();
    }

    public static class CountWrapper {

        private long count;

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}