// A little segmented meter: filled slots are busy, empty slots are free capacity.
export default function CapacityMeter({ load, max }) {
  return (
    <div className="flex gap-1">
      {Array.from({ length: max }).map((_, i) => (
        <span
          key={i}
          className={`h-2 w-6 rounded-full ${i < load ? 'bg-teal-400' : 'bg-slate-700/70'}`}
        />
      ))}
    </div>
  )
}
