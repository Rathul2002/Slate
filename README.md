# ✦ Slate

<p align="center">

**A modern, native UI framework for Java — built around Java, XML, and CSS.**

</p>

<p align="center">

`Java` · `XML` · `CSS` · `JavaFX` · `Native Desktop`

</p>

<p align="center">

> **Bring the developer experience of modern UI frameworks to native Java applications.**

</p>

---

## 🌌 What is Slate?

**Slate** is an experimental native Java desktop UI framework inspired by the developer experience of **React** and **Spring Boot**.

Its goal is to make native Java desktop development feel **modern, declarative, component-driven, and enjoyable** — without turning Java desktop development into web development.

```text
┌───────────────────────────────────────────────┐
│                    SLATE                      │
├───────────────────────────────────────────────┤
│  Java        → Application logic              │
│  XML         → Declarative UI structure       │
│  CSS         → Styling                        │
│  Slate Core  → Components, lifecycle,         │
│                rendering & framework logic    │
└───────────────────────────────────────────────┘
```

---

## ✨ Why Slate?

Modern UI development has moved toward **declarative interfaces, reusable components, styling systems, lifecycle management, and reactive updates**.

Slate explores how those ideas can be brought to the **Java desktop ecosystem** while keeping the application native.

Instead of mixing UI construction and application logic together, Slate separates responsibilities so developers can focus on the application itself.

---

# 🎯 Vision



### The idea in one sentence

> **Make native Java development feel as structured and productive as modern component-based application development.**

---

# 🧭 What Makes Slate Different?

### 01 · 🖥️ Native First

Slate is designed for native desktop applications.

It does **not** depend on:

- WebView
- Chromium
- Browser rendering
- Localhost servers
- JavaScript as the UI runtime

JavaFX is currently the initial rendering backend, hidden behind Slate's renderer abstraction.

### 02 · 🧩 Declarative UI

Instead of constructing every UI element manually in Java:

```java
Button button = new Button();
button.setText("Login");
```

Slate aims for:

```xml
<Button>Login</Button>
```

The XML describes **what the UI should contain** while Slate manages how that UI is created and rendered.

### 03 · 🧱 Component Driven

UI is built from reusable components.

A component can have its own:

`XML structure` · `Styles` · `Props` · `Lifecycle` · `State` · `Children`

### 04 · 🎨 CSS-Based Styling

Slate uses CSS as the styling layer, keeping presentation separate from application logic.

### 05 · 🔌 Renderer Abstraction

The Slate core is designed not to depend directly on JavaFX.

```text
                ┌───────────────┐
                │   Slate Core  │
                └───────┬───────┘
                        │
               Renderer Abstraction
                 ┌──────┴──────┐
                 ▼             ▼
              JavaFX      Future Renderer
```

This leaves room for alternative rendering implementations as the framework evolves.

---

# 🏗️ Architecture



### Core pipeline

```text
Application
     │
     ▼
Slate.run(...)
     │
     ▼
SlateApplication
     │
     ▼
Application Bootstrap
     │
 ┌───┴───────────────┐
 ▼                   ▼
Component Scanner   Resource Loader
 └─────────┬─────────┘
           ▼
     Component System
           │
           ▼
      Component Tree
           │
           ▼
     Renderer Layer
           │
           ▼
         JavaFX
```

---

# 🧩 Component Model



### A simple component

```xml
<View class="home">
    <Heading>Welcome</Heading>

    <View class="content">
        <Text>Welcome to my application.</Text>
        <Button>Continue</Button>
    </View>
</View>
```

The root `<View>` provides a generic composition container.

---

# 🎨 Styling



### Styling pipeline

```text
Global CSS
    +
Component CSS
    +
Inline Styles
    ↓
Final Component Styling
```

Component-level styling is intended to have higher precedence than global styling.

---

# 🧑‍💻 Developer Experience



```text
        ┌──────────────────────────┐
        │       Slate Project      │
        ├──────────────────────────┤
        │ UI       → XML           │
        │ Style    → CSS           │
        │ Logic    → Java          │
        │ Runtime  → Slate         │
        └──────────────────────────┘
```

The goal is a project structure that is immediately understandable: **UI, style, logic, and framework responsibilities stay distinct.**

---

# 📦 Current Implementation

## 🚧 Current Status

> **Stage:** Core Framework + JavaFX Renderer — Active Development

### At a Glance

| Area | Status |
|---|---|
| Application bootstrap | ✅ Complete |
| Component architecture | ✅ Complete |
| Rendering architecture | ✅ Complete |
| JavaFX backend | ✅ Functional |
| Props & content projection | ✅ Implemented |
| State & reactivity | 🟡 Next major phase |
| CSS & layout | 🟡 Planned |
| Tooling & distribution | 🟡 Planned |

### Implementation Progress

**69 implemented** · **29 remaining (the number of remaining items may increase as development progresses)** · **55% of tracked roadmap items complete**

`███████████░░░░░░░░░` **55%**

The framework has moved beyond its initial bootstrap stage. Slate now has a working foundation for application startup, component discovery and loading, component trees, content projection, renderer abstraction, a JavaFX backend, native mounting, basic events, and props.

## ✅ Implemented

### 🚀 Application & Bootstrap
- [x] Slate application bootstrap
- [x] @SlateApp validation
- [x] Duplicate startup prevention
- [x] ApplicationContext
- [x] @Root discovery
- [x] Single-root validation
- [x] Root onInit hook

### 📚 Resources & Classpath
- [x] Application-aware ResourceLoader
- [x] ResourceScanner
- [x] Directory/JAR ClasspathScanner
- [x] Secure XML parsing

### 🧩 Component System
- [x] SlateNode
- [x] SlateTextNode
- [x] Filename-based component identity
- [x] Window.xml special bootstrap handling
- [x] ComponentDefinition
- [x] ComponentRegistry
- [x] ComponentResolver
- [x] ComponentLoader
- [x] ComponentScanner
- [x] Optional Java behavior classes
- [x] Visual-only components
- [x] ComponentInstance
- [x] ComponentTreeBuilder
- [x] ComponentTreeNode
- [x] Circular component detection
- [x] ComponentInputs
- [x] Separate component internalRoot

### 📦 Content Projection
- [x] ContentProjection
- [x] <Content/> projection
- [x] <Content class="..."/> projection
- [x] Whitespace-separated class matching
- [x] AND semantics for multiple required classes
- [x] Non-consuming projection

### 🔌 Element & Renderer Architecture
- [x] SlateElementProvider
- [x] ServiceLoader provider discovery
- [x] BuiltInElementRegistry
- [x] Renderer abstraction
- [x] RendererProvider discovery
- [x] JavaFX backend
- [x] JavaFX element registry
- [x] JavaFX element renderer contract

### 🖥️ JavaFX Rendering
- [x] Window Stage/Scene creation
- [x] View rendering
- [x] Text rendering
- [x] Button rendering
- [x] Static property support
- [x] Window title
- [x] Window dimensions
- [x] Window resizable
- [x] Window min/max dimensions
- [x] Window maximized
- [x] Window fullscreen
- [x] Window validation
- [x] JavaFX toolkit lifecycle abstraction
- [x] JavaFX application-thread execution
- [x] Component native mount tracking
- [x] Native parent-based mount state
- [x] Component native ownership propagation
- [x] Minimal Button.onClick
- [x] declarationOwner runtime metadata
- [x] Declaration-based event resolution

### 🏷️ Props & Module Integration
- [x] @Prop annotation
- [x] ComponentPropBinder
- [x] Runtime Prop snapshots
- [x] Prop type conversion
- [x] Prop update rebinding
- [x] module-info provider declarations
- [x] META-INF/services provider descriptors

## 🔭 In Progress / Planned

### 🧠 State & Reactivity
- [ ] Component-owned State
- [ ] State identity model for repeated projected occurrences
- [ ] Explicit independent State for projected occurrences
- [ ] Dependency tracking
- [ ] Scheduling/batching
- [ ] Automatic re-evaluation
- [ ] Reconciliation/diffing
- [ ] Native incremental updates
- [ ] Stable keys
- [ ] Dynamic components

### ⚡ Lifecycle & Events
- [ ] Component lifecycle
- [ ] Full Slate event abstraction
- [ ] Event payloads
- [ ] Event propagation/capture model
- [ ] Event cleanup lifecycle

### 🎨 CSS & Layout
- [ ] CSS parser
- [ ] CSS cascade
- [ ] CSS scoping
- [ ] Final layout engine

### ✨ Effects & Windows
- [ ] Effects
- [ ] Effect cleanup
- [ ] Multiple windows

### 🛠️ Tooling, Testing & Distribution
- [ ] Complete module-path application workflow
- [ ] Strict named-module @Prop reflection verification
- [ ] Zero-configuration Maven launcher
- [ ] Automatic packaging tooling
- [ ] Dedicated automated test suite
- [ ] Final artifact/module split
- [ ] Publishing pipeline

### Next Major Milestone

The next major step is to make Slate **reactive and stateful**: component-owned state, dependency tracking, automatic re-evaluation, reconciliation, incremental native updates, richer events, and dynamic components.

That foundation will then support the larger UI platform pieces — **CSS, layout, effects, multiple windows, testing, tooling, packaging, and publishing**.



# 🗺️ Roadmap



```text
Foundation
    │
    ├── Bootstrap ✅
    ├── Components ✅
    ├── Rendering ✅
    └── Props ✅
          │
          ▼
Reactivity
    │
    ├── State
    ├── Events
    ├── Effects
    └── Reconciliation
          │
          ▼
UI Platform
    │
    ├── CSS
    ├── Layout
    ├── Dynamic Components
    └── Multiple Windows
          │
          ▼
Developer Ecosystem
    │
    ├── Tooling
    ├── Testing
    ├── Packaging
    └── Publishing
```

---

# 🌐 Long-Term Vision

Slate aims to make building native Java desktop applications feel more structured and developer-friendly while keeping the performance and capabilities of a native application.
```text
Java + XML + CSS = Slate
```

---

# 🧠 Philosophy

Slate follows a few core principles:
**Native over web.**
**Declarative over repetitive.**
**Components over monoliths.**
**Separation of UI, style, and logic.**
**Framework abstractions over renderer-specific code.**
**Developer experience matters.**

---

# 💡 The Bigger Idea

Java has a powerful ecosystem for backend development, enterprise applications, tooling, and large-scale systems.
Slate explores a different question:
> **What if building a native Java desktop application could feel as modern as building a web application with today's component-based frameworks?**

That question is the foundation of Slate.
It is an experiment in bringing modern framework design, declarative UI, component architecture, and a strong developer experience to the native Java desktop world.

---

# 🛠️ Project Status

Slate is an **actively evolving personal open-source project**. The architecture and APIs may change significantly as the framework develops.

The current priority is to finish the framework's **reactivity, event model, styling system, testing, tooling, and packaging** on top of the existing core.

---

<p align="center">

**Built from scratch with Java.**

*Exploring what a modern native Java UI framework could look like.*

</p>
