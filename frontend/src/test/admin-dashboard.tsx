import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

function AdminDashboard() {
  return (
    <main>
      <h1>Admin Dashboard</h1>
      <p>Welcome back, Administrator.</p>
      <button type="button">Create Event</button>
    </main>
  );
}

describe("AdminDashboard", () => {
  it("renders the dashboard heading and action", () => {
    render(<AdminDashboard />);

    expect(screen.getByRole("heading", { name: "Admin Dashboard" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Create Event" })).toBeInTheDocument();
  });
});
