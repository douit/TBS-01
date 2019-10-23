/* fixing: $ is not defined */
declare var $: any;
declare var jQuery: any;

/* SystemJS module definition */
declare var module: NodeModule;
interface NodeModule {
  id: string;
}
