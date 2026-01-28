(function () {
    function syncRow(row) {
        const closed = row.querySelector('.closed-toggle');
        const openSel = row.querySelector('.open-select');
        const closeSel = row.querySelector('.close-select');
        if (!closed || !openSel || !closeSel) return;

        const isClosed = closed.checked;

        // 휴무면 시간 선택 비활성화 + 값 비우기
        openSel.disabled = isClosed;
        closeSel.disabled = isClosed;

        if (isClosed) {
            openSel.value = "";
            closeSel.value = "";
        }
    }

    function init() {
        // header row는 .closed-toggle이 없어서 자동 스킵됨
        document.querySelectorAll('.schedule-row').forEach((row) => {
            syncRow(row);
            const closed = row.querySelector('.closed-toggle');
            if (closed) {
                closed.addEventListener('change', () => syncRow(row));
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
