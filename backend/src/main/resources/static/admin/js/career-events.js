document.addEventListener("DOMContentLoaded", () => {

        const events = getEvents();
        const currentId = eventId.value;

        const eventData = {
            id: currentId || String(Date.now()),
            title: eventTitle.value.trim(),
            type: eventType.value,
            date: eventDate.value,
            time: eventTime.value,
            location: eventLocation.value.trim(),
            organizer: eventOrganizer.value.trim(),
            description: eventDescription.value.trim(),
            updatedAt: new Date().toISOString()
        };

        if (currentId) {
            const index = events.findIndex(event => event.id === currentId);
            if (index >= 0) {
                events[index] = eventData;
                showMessage("Event updated.");
            }
        } else {
            events.push(eventData);
            showMessage("Event created.");
        }

        saveEvents(events);
        clearForm();
        renderEvents();
    }

    function viewEvent(id) {
        const event = getEvents().find(item => item.id === id);
        if (!event) return;

        alert(
            `Title: ${event.title}\n` +
            `Type: ${event.type}\n` +
            `Date: ${event.date}\n` +
            `Time: ${event.time}\n` +
            `Location: ${event.location}\n` +
            `Organizer: ${event.organizer}\n\n` +
            `Description:\n${event.description || "-"}`
        );
    }

    function editEvent(id) {
        const event = getEvents().find(item => item.id === id);
        if (!event) return;

        eventId.value = event.id;
        eventTitle.value = event.title;
        eventType.value = event.type;
        eventDate.value = event.date;
        eventTime.value = event.time;
        eventLocation.value = event.location;
        eventOrganizer.value = event.organizer;
        eventDescription.value = event.description || "";

        formTitle.textContent = "Edit Event";
        saveEventBtn.textContent = "Update Event";
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function deleteEvent(id) {
        if (!confirm("Are you sure you want to delete this event?")) return;

        const events = getEvents().filter(event => event.id !== id);
        saveEvents(events);
        renderEvents();
        showMessage("Event deleted.");
    }

    saveEventBtn.addEventListener("click", createOrUpdateEvent);
    cancelEditBtn.addEventListener("click", () => {
        clearForm();
        showMessage("");
    });

    renderEvents();
});