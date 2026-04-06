# Entity Relationships

## Core Entities

### **Hostel** ↔ **Room** (One-to-Many)

- A **Hostel** contains multiple **Rooms**
- Each **Room** belongs to exactly one **Hostel**
- Rooms inherit gender restrictions from their hostel
- Cascade delete: removing a hostel deletes all its rooms

### **Student** ↔ **StudentPreference** (One-to-Many, Embedded)

- A **Student** can have multiple **StudentPreferences** (ordered list)
- Preferences are embedded entities stored in a separate table
- Preferences specify either hostel name or room capacity requirements

### **Student** ↔ **Allocation** (One-to-Many)

- A **Student** can have multiple **Allocations** across different cycles
- Each **Allocation** belongs to exactly one **Student**
- Only one allocation per student can have status `ALLOCATED` at a time

### **Room** ↔ **Allocation** (One-to-Many, Optional)

- A **Room** can have multiple **Allocations** (students assigned to it)
- Each **Allocation** may reference one **Room** (nullable for waitlisted students)
- Room tracks `occupied` count based on current allocations

### **AllocationCycle** ↔ **Allocation** (One-to-Many, Implicit)

- An **AllocationCycle** groups multiple **Allocations** by `cycleId`
- Each **Allocation** references its cycle via `allocationCycleId`
- Cycles track metadata: strategy used, start/completion times, and results summary

### **AllocationCycle** ↔ **AuditLog** (One-to-Many, Implicit)

- An **AllocationCycle** can have multiple **AuditLog** entries
- Each **AuditLog** may reference a cycle via `allocationCycleId`
- Logs track all actions: cycle start/completion, reallocations, promotions, etc.

## Key Constraints

- **Gender matching**: Students can only be allocated to rooms in hostels matching their gender
- **Capacity limits**: Rooms enforce max capacity (1-10 students)
- **Unique matric numbers**: Each student has a unique matriculation number
- **Status exclusivity**: A student cannot have multiple `ALLOCATED` status allocations simultaneously

## Link to Slides PDF
https://drive.google.com/file/d/18KNDFqS8kd9fPd_J_k4Rh_JPlInT0rlN/view?usp=drive_link

Hosted at https://hostel-allocation-api-1.onrender.com
