import React from "react";

type Event = {
	id: number;
	title: string;
	date: string;
	location: string;
	status: "Upcoming" | "Completed";
};

const events: Event[] = [
	{
		id: 1,
		title: "Community Music Night",
		date: "June 18, 2025",
		location: "Central Park",
		status: "Upcoming",
	},
	{
		id: 2,
		title: "Design Workshop",
		date: "May 24, 2025",
		location: "City Arts Center",
		status: "Completed",
	},
	{
		id: 3,
		title: "Local Food Festival",
		date: "July 5, 2025",
		location: "Riverside Square",
		status: "Upcoming",
	},
];

export default function UserDashboard() {
	const upcomingCount = events.filter((event) => event.status === "Upcoming").length;

	return (
		<main style={styles.page}>
			<header style={styles.header}>
				<div>
					<p style={styles.eyebrow}>GOEVENT</p>
					<h1 style={styles.title}>Welcome back, Isuru</h1>
					<p style={styles.subtitle}>Manage your event plans in one place.</p>
				</div>
				<button style={styles.button} type="button">
					Explore events
				</button>
			</header>

			<section style={styles.stats} aria-label="Event summary">
				<article style={styles.card}>
					<p style={styles.statLabel}>Upcoming events</p>
					<strong style={styles.statValue}>{upcomingCount}</strong>
				</article>
				<article style={styles.card}>
					<p style={styles.statLabel}>Events attended</p>
					<strong style={styles.statValue}>8</strong>
				</article>
				<article style={styles.card}>
					<p style={styles.statLabel}>Saved events</p>
					<strong style={styles.statValue}>12</strong>
				</article>
			</section>

			<section style={styles.eventsSection}>
				<div style={styles.sectionHeading}>
					<h2 style={styles.sectionTitle}>Your events</h2>
					<a href="#all-events" style={styles.link}>
						View all
					</a>
				</div>
				<div style={styles.eventList}>
					{events.map((event) => (
						<article key={event.id} style={styles.eventCard}>
							<div style={styles.eventDate} aria-hidden="true">
								{event.date.split(" ")[0].slice(0, 3)}
							</div>
							<div style={styles.eventDetails}>
								<h3 style={styles.eventTitle}>{event.title}</h3>
								<p style={styles.eventMeta}>
									{event.date} <span aria-hidden="true">·</span> {event.location}
								</p>
							</div>
							<span
								style={{
									...styles.badge,
									...(event.status === "Upcoming" ? styles.upcomingBadge : styles.completedBadge),
								}}
							>
								{event.status}
							</span>
						</article>
					))}
				</div>
			</section>
		</main>
	);
}

const styles: Record<string, React.CSSProperties> = {
	page: {
		maxWidth: 1000,
		margin: "0 auto",
		padding: "48px 24px",
		color: "#182230",
		fontFamily: "Inter, system-ui, sans-serif",
	},
	header: {
		display: "flex",
		justifyContent: "space-between",
		alignItems: "center",
		gap: 24,
		marginBottom: 32,
	},
	eyebrow: { margin: "0 0 8px", color: "#635bff", fontSize: 12, fontWeight: 700, letterSpacing: 1.5 },
	title: { margin: 0, fontSize: 32, lineHeight: 1.2 },
	subtitle: { margin: "8px 0 0", color: "#667085" },
	button: {
		border: 0,
		borderRadius: 8,
		padding: "12px 18px",
		color: "white",
		background: "#635bff",
		fontWeight: 600,
		cursor: "pointer",
	},
	stats: { display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: 16 },
	card: { padding: 20, border: "1px solid #eaecf0", borderRadius: 12, background: "#fff" },
	statLabel: { margin: 0, color: "#667085", fontSize: 14 },
	statValue: { display: "block", marginTop: 10, fontSize: 28 },
	eventsSection: { marginTop: 40 },
	sectionHeading: { display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 },
	sectionTitle: { margin: 0, fontSize: 21 },
	link: { color: "#635bff", fontSize: 14, fontWeight: 600, textDecoration: "none" },
	eventList: { display: "grid", gap: 12 },
	eventCard: {
		display: "flex",
		alignItems: "center",
		gap: 16,
		padding: 16,
		border: "1px solid #eaecf0",
		borderRadius: 12,
		background: "#fff",
	},
	eventDate: {
		display: "grid",
		placeItems: "center",
		width: 48,
		height: 48,
		flexShrink: 0,
		borderRadius: 10,
		color: "#635bff",
		background: "#f0efff",
		fontWeight: 700,
	},
	eventDetails: { flex: 1 },
	eventTitle: { margin: 0, fontSize: 16 },
	eventMeta: { margin: "6px 0 0", color: "#667085", fontSize: 14 },
	badge: { padding: "6px 10px", borderRadius: 999, fontSize: 12, fontWeight: 600 },
	upcomingBadge: { color: "#067647", background: "#ecfdf3" },
	completedBadge: { color: "#475467", background: "#f2f4f7" },
};
