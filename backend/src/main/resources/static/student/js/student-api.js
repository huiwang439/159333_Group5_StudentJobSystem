/**
 * Shared helpers for student frontend — studentType, jobs API, logos.
 */
(function (global) {
  var API_BASE = "http://localhost:8080";

  var STUDENT_TYPE_OPTIONS = [
    { value: "UNDERGRADUATE", label: "University Student (enrolled)" },
    { value: "GRADUATE", label: "Graduate Student (enrolled)" },
    { value: "RECENT_GRADUATE", label: "Recent Graduate" }
  ];

  var TARGET_LABELS = {
    ALL: "All students",
    UNDERGRADUATE: "University students",
    GRADUATE: "Graduate students",
    RECENT_GRADUATE: "Recent graduates"
  };

  function firstNonEmpty() {
    for (var i = 0; i < arguments.length; i++) {
      var value = arguments[i];
      if (value !== null && value !== undefined) {
        var text = String(value).trim();
        if (text) return text;
      }
    }
    return "";
  }

  function labelForStudentType(value) {
    var key = String(value || "").toUpperCase();
    var found = STUDENT_TYPE_OPTIONS.find(function (o) {
      return o.value === key;
    });
    return found ? found.label : key || "Student";
  }

  function labelForTargetStudentType(value) {
    var key = String(value || "ALL").toUpperCase();
    return TARGET_LABELS[key] || key;
  }

  function resolveLogoUrl(job) {
    if (!job) return "";
    var raw =
      job.companyLogoUrl ||
      (job.employerProfile && job.employerProfile.logoUrl) ||
      "";
    if (!raw) return "";
    if (String(raw).indexOf("http") === 0) return raw;
    return API_BASE + (String(raw).charAt(0) === "/" ? raw : "/" + raw);
  }

  function jobRecordPk(job) {
    if (!job) return null;
    var id = job.jobId != null ? job.jobId : job.id;
    if (id === null || id === undefined || id === "") return null;
    var n = Number(id);
    return Number.isNaN(n) ? null : n;
  }

  function companyNameFromJob(job) {
    return firstNonEmpty(
      job && job.companyName,
      job && job.employerProfile && job.employerProfile.companyName,
      "Company"
    );
  }

  function authHeaders(token) {
    return { Authorization: "Bearer " + token };
  }

  function authJsonHeaders(token) {
    return {
      "Content-Type": "application/json",
      Authorization: "Bearer " + token
    };
  }

  function unwrapData(result) {
    if (result && result.data !== undefined) return result.data;
    return result;
  }

  async function fetchStudentProfile(token) {
    var response = await fetch(API_BASE + "/students/profile", {
      headers: authHeaders(token)
    });
    if (!response.ok) return null;
    var result = await response.json();
    return unwrapData(result);
  }

  /** audience: mine | ALL | UNDERGRADUATE | GRADUATE | RECENT_GRADUATE */
  function resolveStudentTypeParam(audience, profileStudentType) {
    if (!audience || audience === "mine") {
      return String(profileStudentType || "UNDERGRADUATE").toUpperCase();
    }
    return String(audience).toUpperCase();
  }

  function normalizeEmploymentType(jobType) {
    if (!jobType) return "";
    return String(jobType).trim().toLowerCase();
  }

  function buildJobsUrl(opts) {
    opts = opts || {};
    var studentType = opts.studentType || "ALL";
    var keyword = opts.keyword || "";
    var location = opts.location || "";
    var employmentType = normalizeEmploymentType(opts.employmentType || opts.jobType);
    var fieldOfStudy = opts.fieldOfStudy || opts.industry || "";

    var hasOther =
      !!keyword || !!location || !!employmentType || !!fieldOfStudy;

    if (hasOther) {
      var params = new URLSearchParams();
      if (keyword) params.set("keyword", keyword);
      if (location) params.set("location", location);
      if (employmentType) params.set("employmentType", employmentType);
      if (fieldOfStudy) params.set("fieldOfStudy", fieldOfStudy);
      params.set("studentType", studentType);
      return API_BASE + "/jobs?" + params.toString();
    }

    return (
      API_BASE + "/jobs/student-type/" + encodeURIComponent(studentType)
    );
  }

  function jobMatchesStudentType(job, studentType) {
    if (!job || !studentType) return true;
    var mine = String(studentType).toUpperCase();
    if (mine === "ALL") return true;
    var target = String(job.targetStudentType || "ALL").toUpperCase();
    return target === "ALL" || target === mine;
  }

  function canApplyToJob(job, profileStudentType) {
    if (!job) return false;
    var target = String(job.targetStudentType || "ALL").toUpperCase();
    if (target === "ALL") return true;
    var mine = String(profileStudentType || "UNDERGRADUATE").toUpperCase();
    return target === mine;
  }

  function renderLogoHtml(job, className) {
    var url = resolveLogoUrl(job);
    var cls = className || "job-logo";
    if (url) {
      return (
        '<img class="' +
        cls +
        '" src="' +
        url +
        '" alt="" loading="lazy" onerror="this.style.display=\'none\'" />'
      );
    }
    var initial = companyNameFromJob(job).charAt(0).toUpperCase() || "?";
    return (
      '<div class="' + cls + ' placeholder" aria-hidden="true">' + initial + "</div>"
    );
  }

  global.StudentApi = {
    API_BASE: API_BASE,
    STUDENT_TYPE_OPTIONS: STUDENT_TYPE_OPTIONS,
    TARGET_LABELS: TARGET_LABELS,
    firstNonEmpty: firstNonEmpty,
    labelForStudentType: labelForStudentType,
    labelForTargetStudentType: labelForTargetStudentType,
    resolveLogoUrl: resolveLogoUrl,
    renderLogoHtml: renderLogoHtml,
    jobRecordPk: jobRecordPk,
    companyNameFromJob: companyNameFromJob,
    authHeaders: authHeaders,
    authJsonHeaders: authJsonHeaders,
    unwrapData: unwrapData,
    fetchStudentProfile: fetchStudentProfile,
    resolveStudentTypeParam: resolveStudentTypeParam,
    buildJobsUrl: buildJobsUrl,
    jobMatchesStudentType: jobMatchesStudentType,
    canApplyToJob: canApplyToJob,
    normalizeEmploymentType: normalizeEmploymentType
  };
})(window);
