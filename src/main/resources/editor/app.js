const fileListEl = document.getElementById("fileList");
const statusOutputEl = document.getElementById("statusOutput");
const currentFileLabelEl = document.getElementById("currentFileLabel");
const saveBtn = document.getElementById("saveBtn");
const refreshFilesBtn = document.getElementById("refreshFilesBtn");
const runTestsBtn = document.getElementById("runTestsBtn");
const createFileBtn = document.getElementById("createFileBtn");
const newFileNameInput = document.getElementById("newFileNameInput");
const viewSelectEl = document.getElementById("viewSelect");
const toggleXmlBtn = document.getElementById("toggleXmlBtn");
const formatXmlBtn = document.getElementById("formatXmlBtn");
const xmlPanelEl = document.getElementById("xmlPanel");
const xmlEditorEl = document.getElementById("xmlEditor");
const canvasEl = document.getElementById("canvas");

const modeler = new DmnJS({ container: "#canvas" });
const DEFAULT_DMN = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/"
             xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/"
             id="NewModelDefinitions"
             name="NewModel"
             namespace="https://example.com/dmn/new-model">
  <decision id="Decision_1" name="Decision1">
    <variable id="Decision_1_var" name="Decision1" typeRef="string"/>
    <literalExpression id="LiteralExpression_1">
      <text>"TODO"</text>
    </literalExpression>
  </decision>
  <dmndi:DMNDI>
    <dmndi:DMNDiagram id="DMNDiagram_1"/>
  </dmndi:DMNDI>
</definitions>`;

let currentFile = null;
let files = [];
let currentViews = [];
let activeViewId = "";
let xmlMode = false;

function setStatus(message) {
  statusOutputEl.textContent = message;
}

function appendStatus(message) {
  statusOutputEl.textContent += `\n${message}`;
  statusOutputEl.scrollTop = statusOutputEl.scrollHeight;
}

function setCurrentFile(fileName) {
  currentFile = fileName;
  currentFileLabelEl.textContent = fileName ? `Editing: ${fileName}` : "No file selected";
  const hasFile = Boolean(fileName);
  saveBtn.disabled = !hasFile;
  toggleXmlBtn.disabled = !hasFile;
  formatXmlBtn.disabled = !hasFile;
  viewSelectEl.disabled = !hasFile || xmlMode || currentViews.length === 0;
  renderFileList();
}

function renderFileList() {
  fileListEl.innerHTML = "";
  files.forEach((fileName) => {
    const li = document.createElement("li");
    li.textContent = fileName;
    if (fileName === currentFile) {
      li.classList.add("active");
    }
    li.addEventListener("click", () => openFile(fileName));
    fileListEl.appendChild(li);
  });
}

function viewLabel(view) {
  const type = view && view.type ? String(view.type).toUpperCase() : "VIEW";
  const name = view && view.name ? view.name : (view && view.id ? view.id : "Unnamed");
  return `${type}: ${name}`;
}

function isDrdView(view) {
  return Boolean(view) && String(view.type).toLowerCase() === "drd";
}

function renderViewOptions(views) {
  viewSelectEl.innerHTML = "";
  if (!views.length) {
    const option = document.createElement("option");
    option.value = "";
    option.textContent = "No views";
    viewSelectEl.appendChild(option);
    return;
  }
  views.forEach((view) => {
    const option = document.createElement("option");
    option.value = view.id;
    option.textContent = viewLabel(view);
    viewSelectEl.appendChild(option);
  });
}

function preferredViewId(views) {
  if (!views.length) {
    return "";
  }
  if (activeViewId && views.some((view) => view.id === activeViewId)) {
    return activeViewId;
  }
  const nonDrdView = views.find((view) => !isDrdView(view));
  return (nonDrdView || views[0]).id;
}

async function openViewById(viewId) {
  const view = currentViews.find((candidate) => candidate.id === viewId);
  if (!view) {
    return;
  }
  await modeler.open(view);
  activeViewId = view.id;
  viewSelectEl.value = view.id;
  if (isDrdView(view)) {
    fitCanvasViewport();
  }
}

function fitCanvasViewport() {
  try {
    const activeViewer = modeler.getActiveViewer && modeler.getActiveViewer();
    if (!activeViewer || !activeViewer.get) {
      return;
    }
    const canvas = activeViewer.get("canvas");
    if (canvas && canvas.zoom) {
      canvas.zoom("fit-viewport", "auto");
    }
  } catch (error) {
    // ignore; not all views provide a diagram canvas service
  }
}

function setXmlMode(enabled) {
  xmlMode = enabled;
  xmlPanelEl.classList.toggle("hidden", !xmlMode);
  canvasEl.classList.toggle("hidden", xmlMode);
  toggleXmlBtn.textContent = xmlMode ? "Visual Mode" : "XML Mode";
  viewSelectEl.disabled = xmlMode || !currentFile || currentViews.length === 0;
}

async function loadFiles() {
  setStatus("Loading DMN file list...");
  const response = await fetch("/api/files");
  if (!response.ok) {
    throw new Error(`Failed to list files: ${response.status}`);
  }
  files = await response.json();
  renderFileList();
  if (!currentFile && files.length > 0) {
    await openFile(files[0]);
  } else {
    setStatus(`Loaded ${files.length} DMN file(s).`);
  }
}

async function openFile(fileName) {
  setStatus(`Opening ${fileName}...`);
  const response = await fetch(`/api/files/${encodeURIComponent(fileName)}`);
  if (!response.ok) {
    throw new Error(`Failed to open ${fileName}: ${response.status}`);
  }
  const xml = await response.text();
  xmlEditorEl.value = xml;

  const result = await modeler.importXML(xml);
  currentViews = (modeler.getViews && modeler.getViews()) || [];
  renderViewOptions(currentViews);
  const defaultViewId = preferredViewId(currentViews);
  if (defaultViewId) {
    await openViewById(defaultViewId);
  } else {
    activeViewId = "";
  }

  setCurrentFile(fileName);
  const warningCount = result && result.warnings ? result.warnings.length : 0;
  const viewInfo = activeViewId ? ` view=${activeViewId}` : "";
  setStatus(`Opened ${fileName}. warnings=${warningCount}${viewInfo}`);

  if (!xmlMode && currentViews.length === 1 && isDrdView(currentViews[0])) {
    appendStatus("Tip: This model only has a DRD view. Use XML Mode if the canvas appears empty.");
  }
}

async function saveCurrentFile() {
  if (!currentFile) {
    setStatus("Pick a file first.");
    return;
  }

  setStatus(`Saving ${currentFile}...`);
  let xml;
  if (xmlMode) {
    xml = xmlEditorEl.value;
  } else {
    xml = (await modeler.saveXML({ format: true })).xml;
    xmlEditorEl.value = xml;
  }

  const response = await fetch(`/api/files/${encodeURIComponent(currentFile)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/xml; charset=utf-8" },
    body: xml
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(`Failed to save ${currentFile}: ${response.status} ${body}`);
  }

  setStatus(`Saved ${currentFile}.`);
}

async function createFileFromCurrent() {
  const fileName = newFileNameInput.value.trim();
  if (!fileName) {
    setStatus("Enter a file name (example: my-rules.dmn).");
    return;
  }
  if (!fileName.endsWith(".dmn")) {
    setStatus("File name must end with .dmn");
    return;
  }

  let xml;
  if (!currentFile) {
    xml = DEFAULT_DMN;
    xmlEditorEl.value = xml;
  } else if (xmlMode) {
    xml = xmlEditorEl.value;
  } else {
    xml = (await modeler.saveXML({ format: true })).xml;
    xmlEditorEl.value = xml;
  }

  setStatus(`Creating ${fileName}...`);
  const response = await fetch(`/api/files/${encodeURIComponent(fileName)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/xml; charset=utf-8" },
    body: xml
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(`Failed to create ${fileName}: ${response.status} ${body}`);
  }

  await loadFiles();
  await openFile(fileName);
  newFileNameInput.value = "";
}

async function formatXml() {
  if (!currentFile) {
    setStatus("Pick a file first.");
    return;
  }

  await modeler.importXML(xmlEditorEl.value);
  const formatted = (await modeler.saveXML({ format: true })).xml;
  xmlEditorEl.value = formatted;

  currentViews = (modeler.getViews && modeler.getViews()) || [];
  renderViewOptions(currentViews);
  const defaultViewId = preferredViewId(currentViews);
  if (defaultViewId) {
    await openViewById(defaultViewId);
  }

  setStatus(`Formatted XML for ${currentFile}.`);
}

async function runTests() {
  setStatus("Running mvn clean test... this can take a while.");
  runTestsBtn.disabled = true;
  try {
    const response = await fetch("/api/tests", { method: "POST" });
    if (!response.ok) {
      const body = await response.text();
      throw new Error(`Test run failed: ${response.status} ${body}`);
    }
    const result = await response.json();
    setStatus(`mvn clean test exitCode=${result.exitCode}, timedOut=${result.timedOut}, durationMs=${result.durationMs}`);
    appendStatus(result.output || "(no output)");
  } finally {
    runTestsBtn.disabled = false;
  }
}

saveBtn.addEventListener("click", () => {
  saveCurrentFile().catch((error) => setStatus(`Save error: ${error.message}`));
});

refreshFilesBtn.addEventListener("click", () => {
  loadFiles().catch((error) => setStatus(`Load error: ${error.message}`));
});

createFileBtn.addEventListener("click", () => {
  createFileFromCurrent().catch((error) => setStatus(`Create error: ${error.message}`));
});

viewSelectEl.addEventListener("change", () => {
  openViewById(viewSelectEl.value).catch((error) => setStatus(`View error: ${error.message}`));
});

toggleXmlBtn.addEventListener("click", async () => {
  try {
    if (xmlMode) {
      await modeler.importXML(xmlEditorEl.value);
      currentViews = (modeler.getViews && modeler.getViews()) || [];
      renderViewOptions(currentViews);
      const defaultViewId = preferredViewId(currentViews);
      if (defaultViewId) {
        await openViewById(defaultViewId);
      }
      setXmlMode(false);
      setStatus(`Switched to visual mode for ${currentFile}.`);
      return;
    }

    if (!xmlEditorEl.value && currentFile) {
      xmlEditorEl.value = (await modeler.saveXML({ format: true })).xml;
    }
    setXmlMode(true);
    setStatus(`Switched to XML mode for ${currentFile}.`);
  } catch (error) {
    setStatus(`Mode switch error: ${error.message}`);
  }
});

formatXmlBtn.addEventListener("click", () => {
  formatXml().catch((error) => setStatus(`Format error: ${error.message}`));
});

runTestsBtn.addEventListener("click", () => {
  runTests().catch((error) => setStatus(`Test error: ${error.message}`));
});

window.addEventListener("resize", () => {
  if (!xmlMode) {
    fitCanvasViewport();
  }
});

viewSelectEl.innerHTML = "<option>Loading...</option>";
setXmlMode(false);

loadFiles().catch((error) => setStatus(`Startup error: ${error.message}`));
