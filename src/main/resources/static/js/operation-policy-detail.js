(function () {
    'use strict';

    // ========================================
    // DOM Ready
    // ========================================
    document.addEventListener('DOMContentLoaded', function () {
        initDeactivateButton();
        initDeleteButton();
        initModalControls();
        initKeyboardHandlers();
    });

    // ========================================
    // Deactivate Modal
    // ========================================
    function initDeactivateButton() {
        var deactivateBtn = document.querySelector('.btn-deactivate');
        if (deactivateBtn) {
            deactivateBtn.addEventListener('click', function (e) {
                e.preventDefault();
                openDeactivateModal();
            });
        }
    }

    function openDeactivateModal() {
        // Check which modal exists (blocked or confirm)
        var blockedModal = document.getElementById('deactivateBlockedModal');
        var confirmModal = document.getElementById('deactivateConfirmModal');

        if (blockedModal) {
            showModal(blockedModal);
        } else if (confirmModal) {
            showModal(confirmModal);
        }
    }

    function closeModal(modalId) {
        var modal = document.getElementById(modalId);
        if (modal) {
            hideModal(modal);
        }
    }

    // ========================================
    // Delete Confirmation
    // ========================================
    function initDeleteButton() {
        var deleteForm = document.querySelector('.action-buttons form[action*="/delete"]');
        if (deleteForm) {
            deleteForm.addEventListener('submit', function (e) {
                var confirmed = confirm('이 정책을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
                if (!confirmed) {
                    e.preventDefault();
                }
            });
        }
    }

    // ========================================
    // Modal Controls
    // ========================================
    function initModalControls() {
        // Blocked modal controls
        var blockedModal = document.getElementById('deactivateBlockedModal');
        if (blockedModal) {
            // Cancel button
            var cancelBtn = blockedModal.querySelector('.btn-modal-cancel');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', function () {
                    closeModal('deactivateBlockedModal');
                });
            }

            // Confirm button (just closes the modal for blocked state)
            var confirmBtn = blockedModal.querySelector('.btn-modal-confirm');
            if (confirmBtn) {
                confirmBtn.addEventListener('click', function () {
                    closeModal('deactivateBlockedModal');
                });
            }

            // Backdrop click
            blockedModal.addEventListener('click', function (e) {
                if (e.target === blockedModal) {
                    closeModal('deactivateBlockedModal');
                }
            });
        }

        // Confirm modal controls
        var confirmModal = document.getElementById('deactivateConfirmModal');
        if (confirmModal) {
            // Cancel button
            var cancelBtn = confirmModal.querySelector('.btn-modal-cancel');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', function () {
                    closeModal('deactivateConfirmModal');
                });
            }

            // Backdrop click
            confirmModal.addEventListener('click', function (e) {
                if (e.target === confirmModal) {
                    closeModal('deactivateConfirmModal');
                }
            });
        }
    }

    // ========================================
    // Keyboard Handlers
    // ========================================
    function initKeyboardHandlers() {
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                closeAllModals();
            }
        });
    }

    function closeAllModals() {
        document.querySelectorAll('.modal-overlay').forEach(function (modal) {
            hideModal(modal);
        });
    }

    // ========================================
    // Modal Utilities
    // ========================================
    function showModal(modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function hideModal(modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }

})();
