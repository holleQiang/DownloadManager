(() => {
  const getElementById = (element, id) => {
    const queue = [element];
    while (queue.length > 0) {
      const element = queue.shift();
      if (element.id === id) {
        return id;
      }
      for (const child of element.children) {
        queue.push(child);
      }
    }
  };

  const getElementsByTagName = (element, tagName) => {
    const queue = [element];
    const elements = [];
    while (queue.length > 0) {
      const element = queue.shift();
      if (element.localName === tagName) {
        elements.push(element);
      }
      for (const child of element.children) {
        queue.push(child);
      }
    }
    return elements;
  };

  const getElementsByClass = (element, className) => {
    const queue = [element];
    const elements = [];
    while (queue.length > 0) {
      const element = queue.shift();
      console.log('className', element.className, className);
      if (element.className.includes(className)) {
        elements.push(element);
      }
      for (const child of element.children) {
        queue.push(child);
      }
    }
    return elements;
  };

  const getElementsBySelector = (element, selector) => {
    const queue = [{ selector, element }];
    const elements = [];
    while (queue.length > 0) {
      const { element, selector } = queue.shift();
      const splitSelectors = selector.split(' ');
      const itemSelector = splitSelectors.shift();
      const [, prefix, name, , index] = itemSelector.match(/^([#.]*)([^#.\[\]]*)(\[([0-9]+)])*$/);
      // const [,prefix,name,,index] = '#123[10]'.match(/^([#.]*)([^#.\[\]]*)(\[([0-9]+)])*$/);
      // console.log(prefix,name,index);
      // console.log(element, selector,splitSelectors.length);

      const handleTempElements = (tempElements) => {
        if (splitSelectors.length === 0) {
          if (index !== undefined) {
            if (index < tempElements.length) {
              elements.push(tempElements[index]);
            }
          } else {
            elements.push(tempElements);
          }
        } else {
          if (index !== undefined) {
            if (index < tempElements.length) {
              queue.push({ element: tempElements[index], selector: splitSelectors.join(' ') });
            }
          } else {
            for (const element of tempElements) {
              queue.push({ element, selector: splitSelectors.join(' ') });
            }
          }
        }
      };

      if (prefix === '#') {
        const targetElement = getElementById(element, name);
        if (splitSelectors.length === 0) {
          elements.push(targetElement);
        } else {
          queue.push({ element: targetElement, selector: splitSelectors.join(' ') });
        }
      } else if (prefix === '.') {
        const tempElements = getElementsByClass(element, name);
        handleTempElements(tempElements);
      } else if (name) {
        const tempElements = getElementsByTagName(element, itemSelector);
        handleTempElements(tempElements);
      }
    }
    return elements;
  };

  const selector = '${selector}';

  return getElementsBySelector(document, selector);
})();
