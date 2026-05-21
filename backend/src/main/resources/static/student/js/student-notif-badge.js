(function () {
  var API_BASE = "http://localhost:8080";

  function updateNotificationBadge() {
    var badge = document.getElementById("nav-notif-badge");
    if (!badge) return;
    var token = localStorage.getItem("token");
    if (!token) {
      badge.setAttribute("hidden", "");
      badge.textContent = "";
      return;
    }
    fetch(API_BASE + "/notifications/unread-count", {
      headers: { Authorization: "Bearer " + token }
    })
      .then(function (res) {
        return res.json();
      })
      .then(function (result) {
        var data = result && result.data !== undefined ? result.data : result;
        var n = data && data.unreadCount != null ? Number(data.unreadCount) : 0;
        if (!n || Number.isNaN(n)) {
          badge.setAttribute("hidden", "");
          badge.textContent = "";
          return;
        }
        badge.removeAttribute("hidden");
        badge.textContent = n > 99 ? "99+" : String(n);
      })
      .catch(function () {
        fetch(API_BASE + "/notifications/unread", {
          headers: { Authorization: "Bearer " + token }
        })
          .then(function (res) {
            return res.json();
          })
          .then(function (result) {
            var list = result && result.data !== undefined ? result.data : result;
            var count = Array.isArray(list) ? list.length : 0;
            if (!count) {
              badge.setAttribute("hidden", "");
              badge.textContent = "";
              return;
            }
            badge.removeAttribute("hidden");
            badge.textContent = count > 99 ? "99+" : String(count);
          })
          .catch(function () {
            badge.setAttribute("hidden", "");
            badge.textContent = "";
          });
      });
  }

  window.refreshStudentNotifBadge = updateNotificationBadge;

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", updateNotificationBadge);
  } else {
    updateNotificationBadge();
  }
  setInterval(updateNotificationBadge, 60000);
})();
