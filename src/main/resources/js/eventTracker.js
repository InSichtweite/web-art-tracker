
window.__eventTracker = {
  mousemove: 0,
  mousedown: 0,
  click: 0,
  dblclick: 0,
  wheel: 0,
  keyboard: 0
};

document.addEventListener("mousemove", () => { window.__eventTracker.mousemove++; });
document.addEventListener("mousedown", () => { window.__eventTracker.mousedown++; });
document.addEventListener("click", () => { window.__eventTracker.click++; });
document.addEventListener("dblclick", () => { window.__eventTracker.dblclick++; });
document.addEventListener("wheel", () => { window.__eventTracker.wheel++; });
document.addEventListener("keydown", () => { window.__eventTracker.keyboard++; });
