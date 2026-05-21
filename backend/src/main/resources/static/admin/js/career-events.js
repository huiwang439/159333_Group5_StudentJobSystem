document.addEventListener("DOMContentLoaded", () => {
    if (!requireCareerStaff()) return;

    const eventId = document.getElementById("eventId");
    const eventTitle = document.getElementById("eventTitle");
    const eventType = document.getElementById("eventType");
    const eventDate = document.getElementById("eventDate");
    const eventTime = document.getElementById("eventTime");
    const eventLocation = document.getElementById("eventLocation");
    const eventOrganizer = document.getElementById("eventOrganizer");
    const eventDescription = document.getElementById("eventDescription");

    const saveEventBtn = document.getElementById("saveEventBtn");
    const cancelEditBtn = document.getElementById("cancelEditBtn");
    const eventsTableBody = document.getElementById("eventsTableBody");
    const eventsMessage = document.getElementById("eventsMessage");
    const formTitle = document.getElementById("formTitle");

    const eventListView = document.getElementById("eventListView");
    const eventDetailView = document.getElementById("eventDetailView");
    const eventDetailCard = document.getElementById("eventDetailCard");
    const backToEventsBtn = document.getElementById("backToEventsBtn");

    function showMessage(message, isError = false) {
        eventsMessage.textContent = message || "";
        eventsMessage.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function formatValue(value) {
        if (value === null || value === undefined || value === "") return "-";
        return value;
    }

    function formatDateTime(value) {
        if (!value) return { date: "-", time: "-" };

        const dateObj = new Date(value);

        if (Number.isNaN(dateObj.getTime())) {
            return { date: "-", time: "-" };
        }

        return {
            date: dateObj.toISOString().slice(0, 10),
            time: dateObj.toTimeString().slice(0, 5)
        };
    }

    function buildEventDateTime() {
        if (!eventDate.value || !eventTime.value) {
            throw new Error("Date and time are required.");
        }

        return `${eventDate.value}T${eventTime.value}:00`;
    }

    function buildDescription() {
        const typeText = eventType.value || "";
        const descriptionText = eventDescription.value.trim();

        return `[Type: ${typeText}]\n${descriptionText}`;
    }

    function extractType(description) {
        if (!description) return "-";

        const match = description.match(/^\[Type:\s*(.*?)\]/);

        if (match && match[1]) {
            return match[1];
        }

        return "-";
    }

    function extractDescription(description) {
        if (!description) return "";

        return description.replace(/^\[Type:\s*.*?\]\n?/, "").trim();
    }

    function showListView() {
        eventDetailView.classList.add("hidden");
        eventListView.classList.remove("hidden");
        showMessage("");
    }

    function showDetailView() {
        eventListView.classList.add("hidden");
        eventDetailView.classList.remove("hidden");
        showMessage("");
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function clearForm() {
        eventId.value = "";
        eventTitle.value = "";
        eventType.value = "Career Fair";
        eventDate.value = "";
        eventTime.value = "";
        eventLocation.value = "";
        eventOrganizer.value = "";
        eventDescription.value = "";

        formTitle.textContent = "Create Event";
        saveEventBtn.textContent = "Create Event";
    }

    function renderEventDetail(event) {
        const dateTime = formatDateTime(event.eventDate);

        eventDetailCard.innerHTML = `
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">Event ID</span>
                    <span class="detail-value">${formatValue(event.id)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Event Title</span>
                    <span class="detail-value">${formatValue(event.title)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Event Type</span>
                    <span class="detail-value">${formatValue(extractType(event.description))}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Date</span>
                    <span class="detail-value">${dateTime.date}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Time</span>
                    <span class="detail-value">${dateTime.time}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Location / Online Link</span>
                    <span class="detail-value">${formatValue(event.location)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Organizer</span>
                    <span class="detail-value">${formatValue(event.organizer)}</span>
                </div>

                <div class="detail-item full-width">
                    <span class="detail-label">Description</span>
                    <span class="detail-value">${formatValue(extractDescription(event.description))}</span>
                </div>
            </div>
        `;
    }

    function renderEvents(events) {
        eventsTableBody.innerHTML = "";

        if (!events || events.length === 0) {
            eventsTableBody.innerHTML = `
                <tr>
                    <td colspan="7">No career events found</td>
                </tr>
            `;
            return;
        }

        events.forEach(event => {
            const dateTime = formatDateTime(event.eventDate);
            const type = extractType(event.description);

            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${formatValue(event.title)}</td>
                <td>${formatValue(type)}</td>
                <td>${dateTime.date}</td>
                <td>${dateTime.time}</td>
                <td>${formatValue(event.location)}</td>
                <td>${formatValue(event.organizer)}</td>
                <td>
                    <button class="view-event-btn" data-id="${event.id}">View</button>
                    <button class="edit-event-btn" data-id="${event.id}">Edit</button>
                    <button class="delete-event-btn" data-id="${event.id}">Delete</button>
                </td>
            `;

            eventsTableBody.appendChild(tr);
        });

        eventsTableBody.querySelectorAll(".view-event-btn").forEach(btn => {
            btn.addEventListener("click", () => viewEvent(btn.dataset.id));
        });

        eventsTableBody.querySelectorAll(".edit-event-btn").forEach(btn => {
            btn.addEventListener("click", () => editEvent(btn.dataset.id));
        });

        eventsTableBody.querySelectorAll(".delete-event-btn").forEach(btn => {
            btn.addEventListener("click", () => deleteEvent(btn.dataset.id));
        });
    }

    async function loadEvents() {
        try {
            showMessage("Loading events...");
            const events = await apiGet("/career-events/admin");
            renderEvents(events);
            showMessage("Events loaded.");
        } catch (error) {
            renderEvents([]);
            showMessage(error.message, true);
        }
    }

    async function createOrUpdateEvent() {
        try {
            if (!eventTitle.value.trim()) {
                throw new Error("Event title is required.");
            }

            const body = {
                title: eventTitle.value.trim(),
                description: buildDescription(),
                location: eventLocation.value.trim(),
                eventDate: buildEventDateTime(),
                organizer: eventOrganizer.value.trim()
            };

            if (eventId.value) {
                await apiPut(`/career-events/${eventId.value}`, body);
                showMessage("Event updated.");
            } else {
                await apiPost("/career-events", body);
                showMessage("Event created.");
            }

            clearForm();
            await loadEvents();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function viewEvent(id) {
        try {
            showMessage("Loading event details...");

            const event = await apiGet(`/career-events/${id}`);

            renderEventDetail(event);
            showDetailView();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function editEvent(id) {
        try {
            const event = await apiGet(`/career-events/${id}`);
            const dateTime = formatDateTime(event.eventDate);

            eventId.value = event.id;
            eventTitle.value = event.title || "";
            eventType.value = extractType(event.description) === "-" ? "Career Fair" : extractType(event.description);
            eventDate.value = dateTime.date === "-" ? "" : dateTime.date;
            eventTime.value = dateTime.time === "-" ? "" : dateTime.time;
            eventLocation.value = event.location || "";
            eventOrganizer.value = event.organizer || "";
            eventDescription.value = extractDescription(event.description);

            formTitle.textContent = "Edit Event";
            saveEventBtn.textContent = "Update Event";

            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function deleteEvent(id) {
        if (!confirm("Are you sure you want to delete this event?")) return;

        try {
            await apiDelete(`/career-events/${id}`);
            showMessage("Event deleted.");
            await loadEvents();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    saveEventBtn.addEventListener("click", createOrUpdateEvent);

    cancelEditBtn.addEventListener("click", () => {
        clearForm();
        showMessage("");
    });

    backToEventsBtn.addEventListener("click", showListView);

    loadEvents();
});