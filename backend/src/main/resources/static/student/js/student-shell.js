(function () {
  var API_BASE = "http://localhost:8080";

  document.body.addEventListener("click", function (event) {
    var logoutBtn = event.target.closest("#logout-btn");
    if (!logoutBtn) return;
    event.preventDefault();
    var token = localStorage.getItem("token");
    fetch(API_BASE + "/auth/logout", {
      method: "POST",
      headers: token ? { Authorization: "Bearer " + token } : {}
    })
      .catch(function () {})
      .finally(function () {
        localStorage.clear();
        window.location.replace("/index/index.html");
      });
  });
})();
