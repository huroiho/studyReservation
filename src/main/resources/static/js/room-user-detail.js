(function() {
    'use strict';

    // ========== Constants ==========
    const DEFAULT_SLOT_MINUTES = 60;
    const DEFAULT_MIN_DURATION_MINUTES = 60;
    const DEFAULT_BOOKING_OPEN_DAYS = 30;
    const DAYS_KR = ['일', '월', '화', '수', '목', '금', '토'];

    // ========== DOM Elements ==========
    const container = document.querySelector('.page-container');
    if (!container) return;

    const roomId = parseInt(container.dataset.roomId, 10);
    const roomName = container.dataset.roomName;
    const roomPrice = parseInt(container.dataset.roomPrice, 10);
    const slotMinutes = parseInt(container.dataset.slotMinutes, 10) || DEFAULT_SLOT_MINUTES;
    const minDurationMinutes = parseInt(container.dataset.minDurationMinutes, 10) || DEFAULT_MIN_DURATION_MINUTES;
    const bookingOpenDays = parseInt(container.dataset.bookingOpenDays, 10) || DEFAULT_BOOKING_OPEN_DAYS;
    const requiredSlots = Math.ceil(minDurationMinutes / slotMinutes);

    const dateInput = document.getElementById('slotDate');
    const checkBtn = document.getElementById('checkSlotsBtn');
    const slotsContainer = document.getElementById('slotsContainer');
    const errorMessage = document.getElementById('errorMessage');
    const successMessage = document.getElementById('successMessage');
    const helpText = document.getElementById('helpText');
    const clearSelectionContainer = document.getElementById('clearSelectionContainer');
    const clearSelectionBtn = document.getElementById('clearSelectionBtn');
    const summaryCard = document.getElementById('summaryCard');
    const reserverForm = document.getElementById('reserverForm');
    const reserveBtn = document.getElementById('reserveBtn');

    // ========== State ==========
    let slots = [];
    let selectedIndices = [];
    let selectedDate = null;

    // ========== Gallery Thumbnail Switching ==========
    const mainImage = document.getElementById('mainImage');
    const thumbnailList = document.getElementById('thumbnailList');

    if (thumbnailList && mainImage) {
        thumbnailList.addEventListener('click', function(e) {
            const btn = e.target.closest('.thumbnail-btn');
            if (!btn) return;

            const url = btn.dataset.url;
            if (url) {
                mainImage.src = url;
                thumbnailList.querySelectorAll('.thumbnail-btn').forEach(function(b) {
                    b.classList.remove('active');
                });
                btn.classList.add('active');
            }
        });
    }

    // ========== Date Setup ==========
    const today = new Date();
    const todayStr = formatDateISO(today);

    const maxDate = new Date(today);
    maxDate.setDate(maxDate.getDate() + bookingOpenDays);
    const maxDateStr = formatDateISO(maxDate);

    dateInput.value = todayStr;
    dateInput.min = todayStr;
    dateInput.max = maxDateStr;

    helpText.textContent = '최소 예약 시간: ' + minDurationMinutes + '분 (' + requiredSlots + '슬롯). 오늘부터 ' + bookingOpenDays + '일 이내 예약 가능.';

    // ========== Utility Functions ==========
    function formatDateISO(date) {
        var yyyy = date.getFullYear();
        var mm = String(date.getMonth() + 1).padStart(2, '0');
        var dd = String(date.getDate()).padStart(2, '0');
        return yyyy + '-' + mm + '-' + dd;
    }

    function isDateWithinBookingWindow(dateStr) {
        return dateStr >= todayStr && dateStr <= maxDateStr;
    }

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
        successMessage.style.display = 'none';
    }

    function hideError() {
        errorMessage.style.display = 'none';
    }

    function showSuccess(message) {
        successMessage.textContent = message;
        successMessage.style.display = 'block';
        errorMessage.style.display = 'none';
    }

    function hideSuccess() {
        successMessage.style.display = 'none';
    }

    function formatTime(timeStr) {
        return timeStr.substring(0, 5);
    }

    function formatDateWithDay(dateStr) {
        var date = new Date(dateStr);
        var y = date.getFullYear();
        var m = String(date.getMonth() + 1).padStart(2, '0');
        var d = String(date.getDate()).padStart(2, '0');
        var dayName = DAYS_KR[date.getDay()];
        return y + '.' + m + '.' + d + ' (' + dayName + ')';
    }

    function formatCurrency(amount) {
        return amount.toLocaleString('ko-KR') + '원';
    }

    function ensureSeconds(timeStr) {
        var parts = timeStr.split(':');
        if (parts.length === 2) {
            return timeStr + ':00';
        }
        return timeStr;
    }

    // ========== Selection Management ==========
    function clearSelection() {
        selectedIndices = [];
        updateSlotDisplay();
        hideSummaryAndForm();
    }

    function hideSummaryAndForm() {
        summaryCard.style.display = 'none';
        reserverForm.style.display = 'none';
        clearSelectionContainer.style.display = 'none';
        reserveBtn.disabled = true;
    }

    function calculateAmount() {
        var totalMinutes = selectedIndices.length * slotMinutes;
        var hours = totalMinutes / 60;
        return Math.round(roomPrice * hours);
    }

    function updateSummary() {
        if (selectedIndices.length === 0 || !selectedDate) {
            hideSummaryAndForm();
            return;
        }

        var sortedIndices = selectedIndices.slice().sort(function(a, b) { return a - b; });
        var startSlot = slots[sortedIndices[0]];
        var endSlot = slots[sortedIndices[sortedIndices.length - 1]];
        var totalMinutes = selectedIndices.length * slotMinutes;

        document.getElementById('summaryRoomName').textContent = roomName;
        document.getElementById('summaryDate').textContent = formatDateWithDay(selectedDate);
        document.getElementById('summaryTime').textContent =
            formatTime(startSlot.startTime) + ' ~ ' + formatTime(endSlot.endTime) +
            ' (' + totalMinutes + '분)';
        document.getElementById('summaryAmount').textContent = formatCurrency(calculateAmount());

        summaryCard.style.display = 'block';
        reserverForm.style.display = 'block';
        clearSelectionContainer.style.display = 'block';

        validateForm();
    }

    function validateForm() {
        var name = document.getElementById('reserverName').value.trim();
        var phone = document.getElementById('reserverPhone').value.trim();
        var email = document.getElementById('reserverEmail').value.trim();

        var isValid = name.length > 0 && phone.length > 0 && email.length > 0 &&
                      selectedIndices.length >= requiredSlots;
        reserveBtn.disabled = !isValid;
    }

    function updateSlotDisplay() {
        document.querySelectorAll('.slot-pill').forEach(function(el, idx) {
            el.classList.remove('selected');
            if (selectedIndices.includes(idx)) {
                el.classList.add('selected');
            }
        });
    }

    // ========== Slot Click Handler ==========
    function handleSlotClick(index) {
        var slot = slots[index];
        if (slot.status !== 'AVAILABLE') return;

        hideError();

        if (selectedIndices.length === 0) {
            // First click: auto-select minimum required slots
            var newSelection = [];
            for (var i = index; i < Math.min(index + requiredSlots, slots.length); i++) {
                if (slots[i].status !== 'AVAILABLE') {
                    showError('최소 예약 시간(' + minDurationMinutes + '분)을 연속으로 선택할 수 없습니다. 다른 시간대를 선택해주세요.');
                    return;
                }
                newSelection.push(i);
            }
            if (newSelection.length < requiredSlots) {
                showError('최소 예약 시간(' + minDurationMinutes + '분)을 선택할 수 없습니다. 슬롯이 부족합니다.');
                return;
            }
            selectedIndices = newSelection;
        } else {
            // Extension: only allow adjacent slots
            var sortedIndices = selectedIndices.slice().sort(function(a, b) { return a - b; });
            var minIdx = sortedIndices[0];
            var maxIdx = sortedIndices[sortedIndices.length - 1];

            if (index === maxIdx + 1) {
                // Extend forward
                selectedIndices.push(index);
            } else if (index === minIdx - 1) {
                // Extend backward
                selectedIndices.push(index);
            } else if (selectedIndices.includes(index)) {
                // Clicked on already selected slot - check if we can deselect
                if (index === minIdx || index === maxIdx) {
                    // Can only deselect edge slots if remaining >= requiredSlots
                    if (selectedIndices.length > requiredSlots) {
                        selectedIndices = selectedIndices.filter(function(i) { return i !== index; });
                    } else {
                        showError('최소 예약 시간(' + minDurationMinutes + '분) 이상 선택해야 합니다.');
                        return;
                    }
                } else {
                    showError('연속된 시간만 선택할 수 있습니다. 가장자리 슬롯만 해제할 수 있습니다.');
                    return;
                }
            } else {
                // Non-adjacent click - restart selection
                var restartSelection = [];
                for (var j = index; j < Math.min(index + requiredSlots, slots.length); j++) {
                    if (slots[j].status !== 'AVAILABLE') {
                        showError('최소 예약 시간(' + minDurationMinutes + '분)을 연속으로 선택할 수 없습니다. 다른 시간대를 선택해주세요.');
                        return;
                    }
                    restartSelection.push(j);
                }
                if (restartSelection.length < requiredSlots) {
                    showError('최소 예약 시간(' + minDurationMinutes + '분)을 선택할 수 없습니다. 슬롯이 부족합니다.');
                    return;
                }
                selectedIndices = restartSelection;
            }
        }

        updateSlotDisplay();
        updateSummary();
    }

    // ========== Slot Rendering ==========
    function renderSlots(data) {
        slots = data;
        selectedIndices = [];
        slotsContainer.innerHTML = '';

        if (!slots || slots.length === 0) {
            slotsContainer.innerHTML = '<p class="slots-empty">예약 가능한 시간이 없습니다. (휴무일)</p>';
            hideSummaryAndForm();
            return;
        }

        var slotGrid = document.createElement('div');
        slotGrid.className = 'slot-pill-grid';

        slots.forEach(function(slot, index) {
            var pill = document.createElement('button');
            pill.type = 'button';
            pill.className = 'slot-pill slot-' + slot.status.toLowerCase();
            pill.textContent = formatTime(slot.startTime);

            if (slot.status === 'AVAILABLE') {
                pill.addEventListener('click', function() {
                    handleSlotClick(index);
                });
            } else {
                pill.disabled = true;
                if (slot.status === 'RESERVED') {
                    pill.title = '이미 예약됨';
                } else {
                    pill.title = '이용 불가 (지난 시간)';
                }
            }

            slotGrid.appendChild(pill);
        });

        slotsContainer.appendChild(slotGrid);
        hideSummaryAndForm();
    }

    // ========== API Calls ==========
    async function fetchSlots() {
        var date = dateInput.value;
        if (!date) {
            showError('날짜를 선택해주세요.');
            return;
        }

        if (!isDateWithinBookingWindow(date)) {
            showError('예약 가능 기간을 초과했습니다. 오늘부터 ' + bookingOpenDays + '일 이내의 날짜를 선택해주세요.');
            return;
        }

        selectedDate = date;
        hideError();
        hideSuccess();
        clearSelection();
        slotsContainer.innerHTML = '<p class="slots-loading">조회 중...</p>';

        try {
            var response = await fetch('/api/rooms/' + roomId + '/slots?date=' + date);
            var result = await response.json();

            if (!response.ok) {
                showError(result.message || '슬롯 조회에 실패했습니다.');
                slotsContainer.innerHTML = '';
                return;
            }

            renderSlots(result.data);
        } catch (err) {
            showError('네트워크 오류가 발생했습니다.');
            slotsContainer.innerHTML = '';
        }
    }

    async function createReservation() {
        if (selectedIndices.length < requiredSlots || !selectedDate) {
            showError('시간을 선택해주세요.');
            return;
        }

        if (!isDateWithinBookingWindow(selectedDate)) {
            showError('예약 가능 기간을 초과했습니다. 오늘부터 ' + bookingOpenDays + '일 이내의 날짜를 선택해주세요.');
            return;
        }

        // TODO: 예약자 정보(reserver fields)는 현재 정책 미정으로 전송하지 않음
        // (로그인 사용자 정보 vs 예약별 입력 여부 논의 필요)
        var name = document.getElementById('reserverName').value.trim();
        var phone = document.getElementById('reserverPhone').value.trim();
        var email = document.getElementById('reserverEmail').value.trim();

        if (!name || !phone || !email) {
            showError('예약자 정보를 모두 입력해주세요.');
            return;
        }

        hideError();
        reserveBtn.disabled = true;
        reserveBtn.textContent = '예약 중...';

        var sortedIndices = selectedIndices.slice().sort(function(a, b) { return a - b; });
        var startSlot = slots[sortedIndices[0]];
        var endSlot = slots[sortedIndices[sortedIndices.length - 1]];

        var startTime = selectedDate + 'T' + ensureSeconds(startSlot.startTime);
        var endTime = selectedDate + 'T' + ensureSeconds(endSlot.endTime);

        var requestBody = {
            roomId: roomId,
            startTime: startTime,
            endTime: endTime
        };

        try {
            var response = await fetch('/api/reservations', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            var result = await response.json();

            if (!response.ok) {
                showError(result.message || '예약에 실패했습니다.');
                reserveBtn.disabled = false;
                reserveBtn.textContent = '예약하기';
                fetchSlots();
                return;
            }

            showSuccess('예약이 완료되었습니다. (예약번호: ' + result.data + ') 10분 내에 결제를 완료해주세요.');
            clearSelection();
            document.getElementById('reserverName').value = '';
            document.getElementById('reserverPhone').value = '';
            document.getElementById('reserverEmail').value = '';
            document.getElementById('reserverCompany').value = '';
            fetchSlots();
        } catch (err) {
            showError('네트워크 오류가 발생했습니다.');
            reserveBtn.disabled = false;
            reserveBtn.textContent = '예약하기';
        }
    }

    // ========== Event Listeners ==========
    checkBtn.addEventListener('click', fetchSlots);
    dateInput.addEventListener('change', fetchSlots);
    clearSelectionBtn.addEventListener('click', clearSelection);
    reserveBtn.addEventListener('click', createReservation);

    document.getElementById('reserverName').addEventListener('input', validateForm);
    document.getElementById('reserverPhone').addEventListener('input', validateForm);
    document.getElementById('reserverEmail').addEventListener('input', validateForm);

})();
